package com.healthybites.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.billing.app.SubscriptionLifecycleJob;
import com.healthybites.billing.events.SubscriptionActivated;
import com.healthybites.billing.events.SubscriptionExpired;
import com.healthybites.billing.infra.PayPalClient.CapturedOrder;
import com.healthybites.billing.infra.PayPalClient.CreatedOrder;
import com.healthybites.shared.domain.Money;
import com.healthybites.support.Seed;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.ResultActions;

/** Planes, checkout con PayPal (simulado), captura verificada e idempotente, cancelación y vencimiento. */
@RecordApplicationEvents
class SubscriptionTests extends IntegrationTest {

    private static final String RETURN_URL = "http://localhost:4210/client/subscription/checkout/return";
    private static final String CANCEL_URL = "http://localhost:4210/client/subscription/checkout/cancel";

    @Autowired
    private Entitlements entitlements;

    @Autowired
    private SubscriptionLifecycleJob lifecycle;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ApplicationEvents events;

    @Test
    void plansArePublicWithPricesAsText() throws Exception {
        mockMvc.perform(get("/subscription-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tier").value("BASIC"))
                .andExpect(jsonPath("$[1].price.amount").value("5.99"))
                .andExpect(jsonPath("$[2].tier").value("VIP"));
    }

    @Test
    void theCurrentTierComesFromThePaidSubscriptions() throws Exception {
        mockMvc.perform(get("/me/subscription").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(jsonPath("$.tier").value("BASIC"))
                .andExpect(jsonPath("$.subscriptionId").doesNotExist());
        mockMvc.perform(get("/me/subscription").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA))))
                .andExpect(jsonPath("$.tier").value("PREMIUM"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.renews").value(true));
        assertThat(entitlements.tierOf(Seed.PEDRO_ID)).isEqualTo(Tier.VIP);
    }

    @Test
    void checkoutAndCaptureActivateTheSubscriptionOnceAndOnlyForItsOwner() throws Exception {
        NewUser buyer = newClient("comprador");
        String orderId = stubOrder();

        checkout(buyer.token(), "PREMIUM")
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.approvalUrl").value("https://www.sandbox.paypal.com/checkoutnow?token=" + orderId));
        verify(payPal)
                .createOrder(anyString(), anyString(), eq(Money.of("5.99", "USD")), eq(RETURN_URL), eq(CANCEL_URL));
        when(payPal.captureOrder(orderId)).thenReturn(captured(subscriptionOf(orderId), "5.99"));

        capture(token(Seed.JUAN), orderId).andExpect(status().isNotFound());

        capture(buyer.token(), orderId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tier").value("PREMIUM"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        capture(buyer.token(), orderId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tier").value("PREMIUM"));

        verify(payPal, times(1)).captureOrder(orderId);
        assertThat(entitlements.tierOf(buyer.id())).isEqualTo(Tier.PREMIUM);
        assertThat(events.stream(SubscriptionActivated.class)).hasSize(1);
        mockMvc.perform(get("/me/payments").header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(jsonPath("$.content[0].status").value("CAPTURED"))
                .andExpect(jsonPath("$.content[0].amount.amount").value("5.99"));
    }

    @Test
    void aCaptureThatDoesNotMatchThePriceActivatesNothing() throws Exception {
        NewUser buyer = newClient("importe");
        String orderId = stubOrder();
        checkout(buyer.token(), "VIP").andExpect(status().isOk());
        when(payPal.captureOrder(orderId)).thenReturn(captured(subscriptionOf(orderId), "0.01"));

        capture(buyer.token(), orderId)
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("payment-mismatch"));
        assertThat(entitlements.tierOf(buyer.id())).isEqualTo(Tier.BASIC);
    }

    @Test
    void upgradingReplacesTheActiveSubscription() throws Exception {
        NewUser buyer = newClient("mejora");
        buy(buyer, "PREMIUM", "5.99");
        buy(buyer, "VIP", "9.99");

        assertThat(entitlements.tierOf(buyer.id())).isEqualTo(Tier.VIP);
        assertThat(jdbc.queryForList(
                        "select status from subscriptions where user_id = ? order by created_at",
                        String.class,
                        buyer.id()))
                .containsExactly("CANCELED", "ACTIVE");
    }

    @Test
    void youCannotBuyWhatYouAlreadyHaveNorTheFreePlan() throws Exception {
        checkout(token(Seed.MARIA), "PREMIUM")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("already-subscribed"));
        checkout(token(Seed.JUAN), "BASIC").andExpect(status().isBadRequest());
        checkout(token(Seed.CARLOS), "PREMIUM").andExpect(status().isForbidden());
        verify(payPal, never()).createOrder(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void cancelingKeepsAccessUntilThePeriodEndsAndThenItExpires() throws Exception {
        NewUser buyer = newClient("baja");
        buy(buyer, "PREMIUM", "5.99");

        mockMvc.perform(post("/me/subscription/cancel").header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.renews").value(false))
                .andExpect(jsonPath("$.tier").value("PREMIUM"));

        jdbc.update(
                "update subscriptions set current_period_end = now() - interval '1 minute' where user_id = ?",
                buyer.id());
        lifecycle.run();

        assertThat(entitlements.tierOf(buyer.id())).isEqualTo(Tier.BASIC);
        assertThat(events.stream(SubscriptionExpired.class))
                .anyMatch(event -> event.userId().equals(buyer.id()));
    }

    @Test
    void adminsSeeSubscriptionsPaymentsAndTheSummary() throws Exception {
        String admin = bearer(token(Seed.ADMIN));
        mockMvc.perform(get("/admin/billing/summary").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeByTier.PREMIUM", Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeByTier.VIP", Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.monthlyRecurringRevenue[0].currency").value("USD"));
        mockMvc.perform(get("/admin/subscriptions").param("tier", "VIP").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.content[*].tier", Matchers.everyItem(Matchers.is("VIP"))))
                .andExpect(jsonPath("$.content[0].customer.email").isNotEmpty())
                .andExpect(jsonPath("$.content[0].customer.name").isNotEmpty());
        mockMvc.perform(get("/admin/payments").param("status", "CAPTURED").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.content[*].status", Matchers.everyItem(Matchers.is("CAPTURED"))))
                .andExpect(jsonPath("$.content[0].customer.email").isNotEmpty());
        mockMvc.perform(get("/admin/billing/summary").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA))))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- utilidades

    private void buy(NewUser buyer, String tier, String amount) throws Exception {
        String orderId = stubOrder();
        checkout(buyer.token(), tier).andExpect(status().isOk());
        when(payPal.captureOrder(orderId)).thenReturn(captured(subscriptionOf(orderId), amount));
        capture(buyer.token(), orderId).andExpect(status().isOk());
    }

    private String stubOrder() {
        String orderId = "ORDER" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        when(payPal.createOrder(anyString(), anyString(), any(), anyString(), anyString()))
                .thenReturn(new CreatedOrder(orderId, "https://www.sandbox.paypal.com/checkoutnow?token=" + orderId));
        return orderId;
    }

    private String subscriptionOf(String orderId) {
        return jdbc.queryForObject(
                "select subscription_id::text from payments where provider_order_id = ?", String.class, orderId);
    }

    private static CapturedOrder captured(String subscriptionId, String amount) {
        return new CapturedOrder("COMPLETED", subscriptionId, "CAPTURE-" + subscriptionId, Money.of(amount, "USD"));
    }

    private ResultActions checkout(String token, String tier) throws Exception {
        return mockMvc.perform(post("/me/subscription/checkout")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tier\": \"" + tier + "\"}"));
    }

    private ResultActions capture(String token, String orderId) throws Exception {
        return mockMvc.perform(post("/me/subscription/checkout/capture")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\": \"" + orderId + "\"}"));
    }
}
