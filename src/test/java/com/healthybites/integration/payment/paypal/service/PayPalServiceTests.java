package com.healthybites.integration.payment.paypal.service;

import com.healthybites.config.properties.PayPalProperties;
import com.healthybites.exception.PaymentProviderException;
import com.healthybites.integration.payment.paypal.dto.OrderCaptureResponse;
import com.healthybites.integration.payment.paypal.dto.OrderResponse;
import com.healthybites.model.entity.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Lo que el cliente de PayPal envía y cómo interpreta respuestas reales (con todos sus campos extra). */
class PayPalServiceTests {

    private static final String BASE = "https://api-m.sandbox.paypal.com";

    private static final String TOKEN_JSON = """
            {"scope": "https://uri.paypal.com/services/payments/payment", "access_token": "A21AA-token",
             "token_type": "Bearer", "app_id": "APP-80W284485P519543T", "expires_in": 31668, "nonce": "2026-11-12"}
            """;

    // Respuesta de PayPal al crear un pedido (resumida, pero con campos que la API no mapea).
    private static final String ORDER_JSON = """
            {"id": "5O190127TN364715T", "status": "CREATED",
             "links": [
               {"href": "https://api-m.sandbox.paypal.com/v2/checkout/orders/5O190127TN364715T", "rel": "self", "method": "GET"},
               {"href": "https://www.sandbox.paypal.com/checkoutnow?token=5O190127TN364715T", "rel": "approve", "method": "GET"}
             ]}
            """;

    // Respuesta de PayPal a una captura (resumida): el importe cobrado está en payments.captures[].
    private static final String CAPTURE_JSON = """
            {"id": "5O190127TN364715T", "status": "COMPLETED",
             "payment_source": {"paypal": {"name": {"given_name": "John", "surname": "Doe"}, "email_address": "c@example.com"}},
             "purchase_units": [{
               "reference_id": "7",
               "shipping": {"address": {"country_code": "PE"}},
               "payments": {"captures": [{
                 "id": "3C679366HH908993F", "status": "COMPLETED",
                 "amount": {"currency_code": "USD", "value": "9.90"},
                 "final_capture": true,
                 "seller_receivable_breakdown": {"gross_amount": {"currency_code": "USD", "value": "9.90"}}
               }]}
             }],
             "payer": {"payer_id": "QYR5Z8XDVJNXQ"},
             "links": [{"href": "https://api-m.sandbox.paypal.com/v2/checkout/orders/5O190127TN364715T", "rel": "self", "method": "GET"}]}
            """;

    private MockRestServiceServer server;
    private PayPalService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new PayPalService(new PayPalProperties("client-id", "client-secret", BASE), builder.build());
    }

    @Test
    void createsTheOrderWithTheServerPriceAndReturnUrls() {
        expectAccessToken();
        server.expect(requestTo(BASE + "/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer A21AA-token"))
                .andExpect(jsonPath("$.intent").value("CAPTURE"))
                .andExpect(jsonPath("$.purchase_units[0].reference_id").value("7"))
                .andExpect(jsonPath("$.purchase_units[0].amount.value").value("9.90"))
                .andExpect(jsonPath("$.purchase_units[0].amount.currency_code").value("USD"))
                .andExpect(jsonPath("$.purchase_units[0].payments").doesNotExist())
                .andExpect(jsonPath("$.application_context.return_url").value("https://app.example/cliente/suscripciones"))
                .andExpect(jsonPath("$.application_context.cancel_url").value("https://app.example/cancelado"))
                .andRespond(withSuccess(ORDER_JSON, MediaType.APPLICATION_JSON));

        OrderResponse order = service.createOrder(subscription(7, 9.9),
                "https://app.example/cliente/suscripciones", "https://app.example/cancelado");

        assertThat(order.getId()).isEqualTo("5O190127TN364715T");
        assertThat(order.getLinks()).anyMatch(link -> "approve".equals(link.getRel()));
        server.verify();
    }

    @Test
    void captureIsIdempotentAtPayPalAndReadsTheCapturedAmount() {
        expectAccessToken();
        server.expect(requestTo(BASE + "/v2/checkout/orders/5O190127TN364715T/capture"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("PayPal-Request-Id", "capture-5O190127TN364715T"))
                .andRespond(withSuccess(CAPTURE_JSON, MediaType.APPLICATION_JSON));

        OrderCaptureResponse capture = service.captureOrder("5O190127TN364715T");

        assertThat(capture.getStatus()).isEqualTo("COMPLETED");
        assertThat(capture.getPurchaseUnits().getFirst().getReferenceId()).isEqualTo("7");
        assertThat(capture.getPurchaseUnits().getFirst().getPayments().getCaptures().getFirst().getAmount().getValue())
                .isEqualTo("9.90");
        server.verify();
    }

    @Test
    void payPalErrorsBecomeAPaymentProviderException() {
        expectAccessToken();
        server.expect(requestTo(BASE + "/v2/checkout/orders/5O190127TN364715T/capture"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"name\": \"UNPROCESSABLE_ENTITY\", \"details\": [{\"issue\": \"ORDER_NOT_APPROVED\"}]}"));

        assertThatThrownBy(() -> service.captureOrder("5O190127TN364715T"))
                .isInstanceOf(PaymentProviderException.class);
    }

    @Test
    void withoutCredentialsPaymentsAreUnavailable() {
        PayPalService unconfigured = new PayPalService(new PayPalProperties(null, null, BASE), RestClient.create(BASE));

        assertThatThrownBy(() -> unconfigured.createOrder(subscription(7, 5.99), "https://a", "https://b"))
                .isInstanceOf(PaymentProviderException.class)
                .hasMessageContaining("no están disponibles");
    }

    @Test
    void amountsAlwaysHaveTwoDecimals() {
        assertThat(PayPalService.formatAmount(9.9)).isEqualTo("9.90");
        assertThat(PayPalService.formatAmount(5.99)).isEqualTo("5.99");
        assertThat(PayPalService.formatAmount(0)).isEqualTo("0.00");
    }

    private void expectAccessToken() {
        String basic = Base64.getEncoder().encodeToString("client-id:client-secret".getBytes(StandardCharsets.UTF_8));
        server.expect(requestTo(BASE + "/v1/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic " + basic))
                .andRespond(withSuccess(TOKEN_JSON, MediaType.APPLICATION_JSON));
    }

    private static Suscripcion subscription(int id, double price) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(id);
        suscripcion.setPrecio(price);
        return suscripcion;
    }
}
