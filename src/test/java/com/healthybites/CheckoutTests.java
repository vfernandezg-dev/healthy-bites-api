package com.healthybites;

import com.healthybites.exception.PaymentProviderException;
import com.healthybites.integration.payment.paypal.dto.Amount;
import com.healthybites.integration.payment.paypal.dto.Capture;
import com.healthybites.integration.payment.paypal.dto.Link;
import com.healthybites.integration.payment.paypal.dto.OrderCaptureResponse;
import com.healthybites.integration.payment.paypal.dto.OrderResponse;
import com.healthybites.integration.payment.paypal.dto.Payments;
import com.healthybites.integration.payment.paypal.dto.PurchaseUnit;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.enums.TipoSuscripcion;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checkout con PayPal (API-8). PayPal está simulado: se comprueba lo que la API le pide y lo que hace con sus
 * respuestas.
 */
class CheckoutTests extends IntegrationTest {

    private static final String RETURN_URL = "http://localhost:4210/cliente/suscripciones";
    private static final String CANCEL_URL = "http://localhost:4210/cliente/suscripciones?checkout=cancelado";

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void theServerBuildsTheReturnUrlsAndLinksTheOrderToTheSubscription() throws Exception {
        Buyer buyer = premiumBuyer("checkout-create");
        String orderId = stubOrder("https://www.sandbox.paypal.com/checkoutnow?token=");

        // Un cliente antiguo aún envía sus URLs: se ignoran.
        mockMvc.perform(post("/checkout/create")
                        .param("suscripcionId", String.valueOf(buyer.suscripcionId()))
                        .param("returnUrl", "https://evil.example/robar")
                        .param("cancelUrl", "https://evil.example/robar")
                        .header("Authorization", bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paypalUrl").value("https://www.sandbox.paypal.com/checkoutnow?token=" + orderId));

        verify(payPalService).createOrder(argThat(s -> s.getId() == buyer.suscripcionId() && s.getPrecio() == 5.99),
                eq(RETURN_URL), eq(CANCEL_URL));
        assertThat(orderIdOf(buyer)).isEqualTo(orderId);
    }

    @Test
    void anApprovalLinkOutsidePayPalIsNeverHandedOut() throws Exception {
        Buyer buyer = premiumBuyer("checkout-host");
        stubOrder("https://paypal.com.evil.example/checkoutnow?token=");

        create(buyer).andExpect(status().isBadGateway());
        assertThat(orderIdOf(buyer)).isNull();
    }

    @Test
    void aPaidSubscriptionCannotBePaidAgain() throws Exception {
        // María (usuario 5) tiene la suscripción 2, PREMIUM y pagada.
        mockMvc.perform(post("/checkout/create").param("suscripcionId", "2")
                        .header("Authorization", bearer(loginAndGetToken("maria.lopez@healthybites.com"))))
                .andExpect(status().isConflict());
        verify(payPalService, never()).createOrder(any(), anyString(), anyString());
    }

    @Test
    void captureActivatesTheSubscriptionOnceAndOnlyForItsOwner() throws Exception {
        Buyer buyer = premiumBuyer("checkout-capture");
        String orderId = stubOrder("https://www.sandbox.paypal.com/checkoutnow?token=");
        create(buyer).andExpect(status().isOk());
        when(payPalService.captureOrder(orderId)).thenReturn(captured(buyer.suscripcionId(), "5.99", "USD"));

        capture(orderId, loginAndGetToken("juan.perez@healthybites.com")).andExpect(status().isNotFound());

        capture(orderId, buyer.token())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.suscripcionId").value(buyer.suscripcionId()));
        assertThat(paymentStateOf(buyer)).isEqualTo("PAGADO");

        // Vuelta repetida (recarga, doble clic): no se vuelve a capturar.
        capture(orderId, buyer.token()).andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(true));
        verify(payPalService, times(1)).captureOrder(orderId);
    }

    @Test
    void aCaptureThatDoesNotMatchThePriceActivatesNothing() throws Exception {
        Buyer buyer = premiumBuyer("checkout-amount");
        String orderId = stubOrder("https://www.sandbox.paypal.com/checkoutnow?token=");
        create(buyer).andExpect(status().isOk());
        when(payPalService.captureOrder(orderId)).thenReturn(captured(buyer.suscripcionId(), "0.01", "USD"));

        capture(orderId, buyer.token()).andExpect(status().isBadGateway());
        assertThat(paymentStateOf(buyer)).isEqualTo("PENDIENTE");
    }

    @Test
    void payPalFailuresAreReportedAsBadGateway() throws Exception {
        Buyer buyer = premiumBuyer("checkout-down");
        when(payPalService.createOrder(any(), anyString(), anyString()))
                .thenThrow(new PaymentProviderException("No se pudo completar la operación con PayPal."));

        create(buyer).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("No se pudo completar la operación con PayPal."));
    }

    @Test
    void unknownOrdersAreNotFound() throws Exception {
        capture("NO-EXISTE", loginAndGetToken("juan.perez@healthybites.com")).andExpect(status().isNotFound());
        verify(payPalService, never()).captureOrder(anyString());
    }

    @Test
    void changingPlanReusesTheUsersSubscription() throws Exception {
        // Antes un segundo alta chocaba con la restricción única por usuario: del plan básico no se podía pasar a otro.
        Registered user = registerClient("cambio-plan");
        String token = user.token();

        int basic = JsonPath.read(subscribe(user, token, "BASICO").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        subscribe(user, token, "BASICO").andExpect(status().isConflict());

        subscribe(user, token, "PREMIUM")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(basic))
                .andExpect(jsonPath("$.precio").value(Suscripcion.precioDe(TipoSuscripcion.PREMIUM)));
        assertThat(jdbc.queryForObject("select estado_pago from suscripcion where id = ?", String.class, basic))
                .isEqualTo("PENDIENTE");
    }

    // ---------------------------------------------------------------- utilidades

    private record Buyer(int usuarioId, String token, int suscripcionId) {
    }

    /** Cliente nuevo con una suscripción PREMIUM pendiente de pago. */
    private Buyer premiumBuyer(String alias) throws Exception {
        Registered user = registerClient(alias);
        String response = subscribe(user, user.token(), "PREMIUM").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new Buyer(user.usuarioId(), user.token(), JsonPath.read(response, "$.id"));
    }

    private ResultActions subscribe(Registered user, String token, String tipo) throws Exception {
        return mockMvc.perform(post("/suscripcion")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"usuarioId": %d, "tipoSuscripcion": "%s"}
                        """.formatted(user.usuarioId(), tipo)));
    }

    private ResultActions create(Buyer buyer) throws Exception {
        return mockMvc.perform(post("/checkout/create")
                .param("suscripcionId", String.valueOf(buyer.suscripcionId()))
                .header("Authorization", bearer(buyer.token())));
    }

    private ResultActions capture(String orderId, String token) throws Exception {
        return mockMvc.perform(post("/checkout/capture").param("orderId", orderId)
                .header("Authorization", bearer(token)));
    }

    /** PayPal crea el pedido y devuelve el enlace de aprobación {@code approvalBase + id}. */
    private String stubOrder(String approvalBase) {
        String orderId = "ORDER" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Link approve = new Link();
        approve.setRel("approve");
        approve.setHref(approvalBase + orderId);
        OrderResponse order = new OrderResponse();
        order.setId(orderId);
        order.setStatus("CREATED");
        order.setLinks(List.of(approve));
        when(payPalService.createOrder(any(), anyString(), anyString())).thenReturn(order);
        return orderId;
    }

    private static OrderCaptureResponse captured(int suscripcionId, String value, String currency) {
        Amount amount = new Amount();
        amount.setValue(value);
        amount.setCurrencyCode(currency);
        Capture capture = new Capture();
        capture.setStatus("COMPLETED");
        capture.setAmount(amount);
        Payments payments = new Payments();
        payments.setCaptures(List.of(capture));
        PurchaseUnit unit = new PurchaseUnit();
        unit.setReferenceId(String.valueOf(suscripcionId));
        unit.setPayments(payments);
        OrderCaptureResponse response = new OrderCaptureResponse();
        response.setStatus("COMPLETED");
        response.setPurchaseUnits(List.of(unit));
        return response;
    }

    private String orderIdOf(Buyer buyer) {
        return jdbc.queryForObject("select paypal_order_id from suscripcion where id = ?", String.class,
                buyer.suscripcionId());
    }

    private String paymentStateOf(Buyer buyer) {
        return jdbc.queryForObject("select estado_pago from suscripcion where id = ?", String.class,
                buyer.suscripcionId());
    }
}
