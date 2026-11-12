package com.healthybites.billing.app;

import com.healthybites.billing.Tier;
import com.healthybites.billing.app.PaymentGateway.CapturedOrder;
import com.healthybites.billing.app.PaymentGateway.CreatedOrder;
import com.healthybites.billing.app.PaymentLedger.CaptureOutcome;
import com.healthybites.billing.app.PaymentLedger.OpenCheckout;
import com.healthybites.billing.app.PaymentLedger.Readiness;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentRepository;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionPlan;
import com.healthybites.billing.domain.SubscriptionPlanRepository;
import com.healthybites.billing.domain.SubscriptionRepository;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.shared.config.AppProperties;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suscripción del usuario actual: checkout con PayPal, captura del pago, cancelación y consulta.
 *
 * <ul>
 *   <li>Las URLs de vuelta las construye el servidor con {@code APP_FRONTEND_URL}.</li>
 *   <li>La captura solo acepta pedidos del usuario, comprueba referencia, importe y moneda, y es idempotente.</li>
 *   <li>Mejorar de plan (PREMIUM → VIP) cancela la suscripción anterior al activar la nueva; el nivel siempre es el
 *       más alto vigente.</li>
 *   <li>Checkout y captura no abren transacción propia: la pasarela de pago se llama entre los pasos cortos de
 *       {@link PaymentLedger}, sin retener conexiones ni bloqueos mientras PayPal responde.</li>
 * </ul>
 */
@Service
public class SubscriptionService {

    static final String RETURN_PATH = "client/subscription/checkout/return";
    static final String CANCEL_PATH = "client/subscription/checkout/cancel";

    private final SubscriptionPlanRepository plans;
    private final SubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final PaymentLedger ledger;
    private final EntitlementsService entitlements;
    private final AppProperties appProperties;
    private final Clock clock;

    public SubscriptionService(
            SubscriptionPlanRepository plans,
            SubscriptionRepository subscriptions,
            PaymentRepository payments,
            PaymentGateway gateway,
            PaymentLedger ledger,
            EntitlementsService entitlements,
            AppProperties appProperties,
            Clock clock) {
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.gateway = gateway;
        this.ledger = ledger;
        this.entitlements = entitlements;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    /** Situación actual: el nivel vigente y la suscripción que lo da (ninguna en el plan básico). */
    public record Current(Tier tier, @Nullable Subscription subscription) {}

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> plans() {
        return plans.findByActiveTrueOrderByPriceAmountAsc();
    }

    @Transactional(readOnly = true)
    public Current current(UUID userId) {
        Optional<Subscription> granting = entitlements.grantingSubscription(userId, clock.instant());
        return new Current(granting.map(Subscription::getTier).orElse(Tier.BASIC), granting.orElse(null));
    }

    /**
     * Crea la suscripción pendiente y el pedido en la pasarela; devuelve la URL donde el usuario aprueba el pago. Si
     * la pasarela falla, la suscripción queda pendiente sin pago y el siguiente checkout la cierra.
     */
    public String checkout(UUID userId, Tier tier) {
        OpenCheckout checkout = ledger.open(userId, tier);
        CreatedOrder order = gateway.createOrder(
                checkout.subscriptionId().toString(),
                checkout.description(),
                checkout.price(),
                appProperties.frontend(RETURN_PATH),
                appProperties.frontend(CANCEL_PATH));
        ledger.recordOrder(checkout, userId, order.id());
        return order.approvalUrl();
    }

    /**
     * Captura el pedido y activa la suscripción. Repetirlo (recarga, doble clic) no cobra ni activa dos veces: la
     * pasarela es idempotente y el pago se aplica con su fila bloqueada. Un rechazo deja el pago FAILED.
     */
    public Current capture(UUID userId, String orderId) {
        if (ledger.prepareCapture(userId, orderId) == Readiness.READY) {
            CapturedOrder captured = gateway.captureOrder(orderId);
            if (ledger.applyCapture(userId, orderId, captured) instanceof CaptureOutcome.Rejected(ApiException error)) {
                throw error;
            }
        }
        return current(userId);
    }

    /** Deja de renovarse; el acceso sigue hasta el fin del período pagado. */
    @Transactional
    public Current cancel(UUID userId) {
        Subscription active = subscriptions
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> ApiException.notFound("no-active-subscription", "No tienes una suscripción activa"));
        active.cancel(clock.instant());
        return current(userId);
    }

    @Transactional(readOnly = true)
    public Page<Payment> payments(UUID userId, Pageable pageable) {
        return payments.findByUserId(userId, pageable);
    }
}
