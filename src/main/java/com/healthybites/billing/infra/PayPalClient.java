package com.healthybites.billing.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthybites.shared.domain.Money;
import com.healthybites.shared.web.ApiException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente de la API Orders v2 de PayPal.
 *
 * <ul>
 *   <li>Importes y URLs de vuelta los decide el servidor; el pedido lleva el id de la suscripción como referencia.</li>
 *   <li>El token OAuth se reutiliza hasta poco antes de caducar.</li>
 *   <li>{@code PayPal-Request-Id} hace idempotentes la creación y la captura: un reintento devuelve el mismo
 *       resultado en lugar de duplicar.</li>
 *   <li>Timeouts cortos y cualquier fallo como 502 ({@code payment-provider-error}); sin credenciales, 503.</li>
 * </ul>
 */
@Slf4j
@Component
public class PayPalClient {

    private static final String UNAVAILABLE = "No se pudo completar la operación con PayPal. Inténtalo más tarde.";
    private static final Set<String> APPROVAL_RELS = Set.of("approve", "payer-action");

    private final PayPalProperties properties;
    private final RestClient http;
    private final Clock clock;
    private @Nullable CachedToken token;

    @Autowired
    public PayPalClient(PayPalProperties properties, Clock clock) {
        this(
                properties,
                RestClient.builder()
                        .baseUrl(properties.apiBase())
                        .requestFactory(withTimeouts())
                        .build(),
                clock);
    }

    PayPalClient(PayPalProperties properties, RestClient http, Clock clock) {
        this.properties = properties;
        this.http = http;
        this.clock = clock;
    }

    /** Pedido creado: su id y la URL donde el comprador lo aprueba. */
    public record CreatedOrder(String id, String approvalUrl) {}

    /** Resultado de la captura. {@code captured} es lo que de verdad se cobró. */
    public record CapturedOrder(
            String status,
            @Nullable String referenceId,
            @Nullable String captureId,
            @Nullable Money captured) {

        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }
    }

    public CreatedOrder createOrder(
            String referenceId, String description, Money amount, String returnUrl, String cancelUrl) {
        OrderRequest body = new OrderRequest(
                "CAPTURE",
                List.of(new PurchaseUnitRequest(
                        referenceId, description, new Amount(amount.currency(), amount.plain()))),
                new PaymentSource(new PayPalSource(new ExperienceContext(
                        "Healthy Bites", "es-PE", "NO_SHIPPING", "PAY_NOW", returnUrl, cancelUrl))));
        OrderResponse order = call(() -> http.post()
                .uri("/v2/checkout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .header("PayPal-Request-Id", "order-" + referenceId)
                .body(body)
                .retrieve()
                .body(OrderResponse.class));
        String approvalUrl = Optional.ofNullable(order.links()).orElse(List.of()).stream()
                .filter(link ->
                        link.rel() != null && APPROVAL_RELS.contains(link.rel().toLowerCase(Locale.ROOT)))
                .map(Link::href)
                .findFirst()
                .orElse(null);
        if (order.id() == null || approvalUrl == null || !isPayPalUrl(approvalUrl)) {
            log.error("PayPal devolvió un pedido sin id o con un enlace de aprobación no válido: {}", approvalUrl);
            throw ApiException.badGateway("payment-provider-error", "PayPal devolvió un enlace de pago no válido.");
        }
        return new CreatedOrder(order.id(), approvalUrl);
    }

    public CapturedOrder captureOrder(String orderId) {
        CaptureResponse response = call(() -> http.post()
                .uri("/v2/checkout/orders/{id}/capture", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .header("PayPal-Request-Id", "capture-" + orderId)
                .retrieve()
                .body(CaptureResponse.class));
        PurchaseUnitResponse unit = Optional.ofNullable(response.purchaseUnits()).orElse(List.of()).stream()
                .findFirst()
                .orElse(null);
        Capture capture =
                unit == null || unit.payments() == null || unit.payments().captures() == null
                        ? null
                        : unit.payments().captures().stream().findFirst().orElse(null);
        Money captured = capture == null || capture.amount() == null
                ? null
                : Money.of(capture.amount().value(), capture.amount().currencyCode());
        return new CapturedOrder(
                String.valueOf(response.status()),
                unit == null ? null : unit.referenceId(),
                capture == null ? null : capture.id(),
                captured);
    }

    /** Solo se redirige a PayPal por HTTPS. */
    public static boolean isPayPalUrl(String href) {
        try {
            URI uri = URI.create(href);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && (host.equals("paypal.com") || host.endsWith(".paypal.com"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private synchronized String accessToken() {
        if (!properties.isConfigured()) {
            throw ApiException.serviceUnavailable(
                    "payments-unavailable", "Los pagos con PayPal no están disponibles en este momento.");
        }
        Instant now = clock.instant();
        if (token != null && now.isBefore(token.expiresAt())) {
            return token.value();
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        String credentials = Base64.getEncoder()
                .encodeToString(
                        (properties.clientId() + ":" + properties.clientSecret()).getBytes(StandardCharsets.UTF_8));
        TokenResponse response = call(() -> http.post()
                .uri("/v1/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .body(form)
                .retrieve()
                .body(TokenResponse.class));
        // Un minuto de margen para no usarlo justo cuando caduca.
        long lifetime = Math.max(0, response.expiresIn() - 60);
        token = new CachedToken(String.valueOf(response.accessToken()), now.plus(Duration.ofSeconds(lifetime)));
        return token.value();
    }

    private static <T> T call(Supplier<@Nullable T> request) {
        T body;
        try {
            body = request.get();
        } catch (RestClientException e) {
            log.warn("Fallo de PayPal: {}", e.getMessage());
            throw ApiException.badGateway("payment-provider-error", UNAVAILABLE);
        }
        if (body == null) {
            throw ApiException.badGateway("payment-provider-error", UNAVAILABLE);
        }
        return body;
    }

    /** Sin timeouts, un PayPal lento dejaría la petición del usuario colgada. */
    private static SimpleClientHttpRequestFactory withTimeouts() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return factory;
    }

    private record CachedToken(String value, Instant expiresAt) {}

    // ---------------------------------------------------------------- JSON de PayPal (solo lo que se usa)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record OrderRequest(
            String intent,
            @JsonProperty("purchase_units") List<PurchaseUnitRequest> purchaseUnits,
            @JsonProperty("payment_source") PaymentSource paymentSource) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record PurchaseUnitRequest(@JsonProperty("reference_id") String referenceId, String description, Amount amount) {}

    record PaymentSource(PayPalSource paypal) {}

    record PayPalSource(@JsonProperty("experience_context") ExperienceContext experienceContext) {}

    record ExperienceContext(
            @JsonProperty("brand_name") String brandName,
            String locale,
            @JsonProperty("shipping_preference") String shippingPreference,
            @JsonProperty("user_action") String userAction,
            @JsonProperty("return_url") String returnUrl,
            @JsonProperty("cancel_url") String cancelUrl) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Amount(@JsonProperty("currency_code") String currencyCode, String value) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OrderResponse(
            @Nullable String id,
            @Nullable String status,
            @Nullable List<Link> links) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Link(String href, @Nullable String rel) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CaptureResponse(
            @Nullable String id,
            @Nullable String status,
            @JsonProperty("purchase_units") @Nullable List<PurchaseUnitResponse> purchaseUnits) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PurchaseUnitResponse(
            @JsonProperty("reference_id") @Nullable String referenceId,
            @Nullable Payments payments) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Payments(@Nullable List<Capture> captures) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Capture(
            @Nullable String id,
            @Nullable String status,
            @Nullable Amount amount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(
            @JsonProperty("access_token") @Nullable String accessToken,
            @JsonProperty("expires_in") long expiresIn) {}
}
