package com.healthybites.billing.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.healthybites.billing.app.PaymentGateway.CapturedOrder;
import com.healthybites.billing.app.PaymentGateway.CreatedOrder;
import com.healthybites.shared.domain.Money;
import com.healthybites.shared.web.ApiException;
import com.healthybites.support.MutableClock;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Lo que el cliente de PayPal envía y cómo interpreta respuestas reales (con sus campos extra). */
class PayPalClientTests {

    private static final String BASE = "https://api-m.sandbox.paypal.com";
    private static final String TOKEN_JSON = """
            {"scope": "https://uri.paypal.com/services/payments/payment", "access_token": "A21AA-token",
             "token_type": "Bearer", "app_id": "APP-80W284485P519543T", "expires_in": 31668, "nonce": "n"}""";
    private static final String ORDER_JSON = """
            {"id": "5O190127TN364715T", "status": "PAYER_ACTION_REQUIRED",
             "payment_source": {"paypal": {}},
             "links": [
               {"href": "https://api-m.sandbox.paypal.com/v2/checkout/orders/5O190127TN364715T", "rel": "self", "method": "GET"},
               {"href": "https://www.sandbox.paypal.com/checkoutnow?token=5O190127TN364715T", "rel": "payer-action", "method": "GET"}
             ]}""";
    private static final String CAPTURE_JSON = """
            {"id": "5O190127TN364715T", "status": "COMPLETED",
             "payment_source": {"paypal": {"email_address": "c@example.com"}},
             "purchase_units": [{
               "reference_id": "sub-7",
               "shipping": {"address": {"country_code": "PE"}},
               "payments": {"captures": [{
                 "id": "3C679366HH908993F", "status": "COMPLETED", "final_capture": true,
                 "amount": {"currency_code": "USD", "value": "9.99"},
                 "seller_receivable_breakdown": {"gross_amount": {"currency_code": "USD", "value": "9.99"}}
               }]}
             }],
             "payer": {"payer_id": "QYR5Z8XDVJNXQ"}}""";

    private MockRestServiceServer server;
    private PayPalClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new PayPalClient(
                new PayPalProperties("client-id", "client-secret", BASE),
                builder.build(),
                new MutableClock(Instant.parse("2026-11-12T10:00:00Z")));
    }

    @Test
    void createsTheOrderWithTheServerAmountUrlsAndIdempotencyKey() {
        expectToken();
        server.expect(requestTo(BASE + "/v2/checkout/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer A21AA-token"))
                .andExpect(header("PayPal-Request-Id", "order-sub-7"))
                .andExpect(jsonPath("$.intent").value("CAPTURE"))
                .andExpect(jsonPath("$.purchase_units[0].reference_id").value("sub-7"))
                .andExpect(jsonPath("$.purchase_units[0].amount.value").value("9.90"))
                .andExpect(jsonPath("$.purchase_units[0].amount.currency_code").value("USD"))
                .andExpect(jsonPath("$.payment_source.paypal.experience_context.return_url")
                        .value("https://app/ok"))
                .andExpect(jsonPath("$.payment_source.paypal.experience_context.user_action")
                        .value("PAY_NOW"))
                .andRespond(withSuccess(ORDER_JSON, MediaType.APPLICATION_JSON));

        CreatedOrder order = client.createOrder(
                "sub-7", "Healthy Bites VIP", Money.of("9.9", "USD"), "https://app/ok", "https://app/ko");

        assertThat(order.id()).isEqualTo("5O190127TN364715T");
        assertThat(order.approvalUrl()).isEqualTo("https://www.sandbox.paypal.com/checkoutnow?token=5O190127TN364715T");
        server.verify();
    }

    @Test
    void captureReadsWhatWasReallyChargedAndReusesTheToken() {
        server.expect(once(), requestTo(BASE + "/v1/oauth2/token"))
                .andRespond(withSuccess(TOKEN_JSON, MediaType.APPLICATION_JSON));
        for (int i = 0; i < 2; i++) {
            server.expect(requestTo(BASE + "/v2/checkout/orders/5O190127TN364715T/capture"))
                    .andExpect(header("PayPal-Request-Id", "capture-5O190127TN364715T"))
                    .andRespond(withSuccess(CAPTURE_JSON, MediaType.APPLICATION_JSON));
        }

        CapturedOrder first = client.captureOrder("5O190127TN364715T");
        client.captureOrder("5O190127TN364715T");

        assertThat(first.isCompleted()).isTrue();
        assertThat(first.referenceId()).isEqualTo("sub-7");
        assertThat(first.captureId()).isEqualTo("3C679366HH908993F");
        assertThat(first.captured()).isEqualTo(Money.of("9.99", "USD"));
        server.verify();
    }

    @Test
    void anApprovalLinkOutsidePayPalIsRejected() {
        expectToken();
        server.expect(requestTo(BASE + "/v2/checkout/orders"))
                .andRespond(withSuccess(
                        ORDER_JSON.replace("www.sandbox.paypal.com", "paypal.com.evil.example"),
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.createOrder("sub-7", "x", Money.of("5.99", "USD"), "https://a", "https://b"))
                .isInstanceOfSatisfying(
                        ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void payPalErrorsAreBadGatewayAndMissingCredentialsAreUnavailable() {
        expectToken();
        server.expect(requestTo(BASE + "/v2/checkout/orders/X/capture"))
                .andRespond(
                        withStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(
                                        "{\"name\": \"UNPROCESSABLE_ENTITY\", \"details\": [{\"issue\": \"ORDER_NOT_APPROVED\"}]}"));

        assertThatThrownBy(() -> client.captureOrder("X"))
                .isInstanceOfSatisfying(
                        ApiException.class, ex -> assertThat(ex.getCode()).isEqualTo("payment-provider-error"));

        PayPalClient unconfigured = new PayPalClient(
                new PayPalProperties(null, null, BASE), RestClient.create(BASE), new MutableClock(Instant.now()));
        assertThatThrownBy(() -> unconfigured.captureOrder("X"))
                .isInstanceOfSatisfying(
                        ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private void expectToken() {
        String basic = Base64.getEncoder().encodeToString("client-id:client-secret".getBytes(StandardCharsets.UTF_8));
        server.expect(requestTo(BASE + "/v1/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic " + basic))
                .andRespond(withSuccess(TOKEN_JSON, MediaType.APPLICATION_JSON));
    }
}
