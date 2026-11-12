package com.healthybites.integration.payment.paypal.service;

import com.healthybites.config.properties.PayPalProperties;
import com.healthybites.exception.PaymentProviderException;
import com.healthybites.integration.payment.paypal.dto.Amount;
import com.healthybites.integration.payment.paypal.dto.ApplicationContext;
import com.healthybites.integration.payment.paypal.dto.OrderCaptureResponse;
import com.healthybites.integration.payment.paypal.dto.OrderRequest;
import com.healthybites.integration.payment.paypal.dto.OrderResponse;
import com.healthybites.integration.payment.paypal.dto.PurchaseUnit;
import com.healthybites.integration.payment.paypal.dto.TokenResponse;
import com.healthybites.model.entity.Suscripcion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

/**
 * Cliente de la API REST de PayPal (Orders v2). Los importes y las URLs de vuelta los decide el servidor; cualquier
 * fallo de PayPal (caído, lento, credenciales, pedido inválido) llega como {@link PaymentProviderException} (502).
 */
@Service
public class PayPalService {

    public static final String CURRENCY = "USD";
    private static final String UNAVAILABLE = "No se pudo completar la operación con PayPal. Inténtalo más tarde.";

    private final PayPalProperties properties;
    private final RestClient paypalClient;

    @Autowired
    public PayPalService(PayPalProperties properties) {
        this(properties, RestClient.builder()
                .baseUrl(properties.apiBase())
                .requestFactory(withTimeouts())
                .build());
    }

    PayPalService(PayPalProperties properties, RestClient paypalClient) {
        this.properties = properties;
        this.paypalClient = paypalClient;
    }

    /** Sin timeouts, un PayPal lento dejaba hilos (y la petición del usuario) colgados indefinidamente. */
    private static SimpleClientHttpRequestFactory withTimeouts() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return factory;
    }

    String getAccessToken() {
        if (!properties.isConfigured()) {
            throw new PaymentProviderException("Los pagos con PayPal no están disponibles en este momento.");
        }
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        String credentials = Base64.getEncoder().encodeToString(
                (properties.clientId() + ":" + properties.clientSecret()).getBytes(StandardCharsets.UTF_8));
        return call(() -> paypalClient.post()
                .uri("/v1/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .body(body)
                .retrieve()
                .body(TokenResponse.class)).getAccessToken();
    }

    /** Crea el pedido por el precio de la suscripción, con su id como referencia y las URLs de vuelta del web-app. */
    public OrderResponse createOrder(Suscripcion suscripcion, String returnUrl, String cancelUrl) {
        Amount amount = new Amount();
        amount.setCurrencyCode(CURRENCY);
        amount.setValue(formatAmount(suscripcion.getPrecio()));

        PurchaseUnit purchaseUnit = new PurchaseUnit();
        purchaseUnit.setReferenceId(suscripcion.getId().toString());
        purchaseUnit.setAmount(amount);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setIntent("CAPTURE");
        orderRequest.setPurchaseUnits(List.of(purchaseUnit));
        orderRequest.setApplicationContext(ApplicationContext.builder()
                .brandName("HealthyBites")
                .returnURL(returnUrl)
                .cancelURL(cancelUrl)
                .build());

        String accessToken = getAccessToken();
        return call(() -> paypalClient.post()
                .uri("/v2/checkout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(orderRequest)
                .retrieve()
                .body(OrderResponse.class));
    }

    /**
     * Captura el pedido. Con {@code PayPal-Request-Id} la operación es idempotente en PayPal: si se repite (reintento,
     * doble clic), devuelve el resultado de la primera captura en vez de un error.
     */
    public OrderCaptureResponse captureOrder(String orderId) {
        String accessToken = getAccessToken();
        return call(() -> paypalClient.post()
                .uri("/v2/checkout/orders/{order_id}/capture", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("PayPal-Request-Id", "capture-" + orderId)
                .retrieve()
                .body(OrderCaptureResponse.class));
    }

    /** Importe con dos decimales, como lo espera PayPal ({@code 9.9} se envía como {@code 9.90}). */
    public static String formatAmount(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static <T> T call(Supplier<T> request) {
        T body;
        try {
            body = request.get();
        } catch (RestClientException e) {
            throw new PaymentProviderException(UNAVAILABLE, e);
        }
        if (body == null) {
            throw new PaymentProviderException(UNAVAILABLE);
        }
        return body;
    }
}
