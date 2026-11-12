package com.healthybites.billing.app;

import com.healthybites.billing.Tier;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentRepository;
import com.healthybites.billing.domain.PaymentStatus;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionPlan;
import com.healthybites.billing.domain.SubscriptionPlanRepository;
import com.healthybites.billing.domain.SubscriptionRepository;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.billing.events.SubscriptionActivated;
import com.healthybites.billing.infra.PayPalClient;
import com.healthybites.billing.infra.PayPalClient.CapturedOrder;
import com.healthybites.billing.infra.PayPalClient.CreatedOrder;
import com.healthybites.shared.config.AppProperties;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
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
 * </ul>
 */
@Slf4j
@Service
public class SubscriptionService {

    static final String RETURN_PATH = "client/subscription/checkout/return";
    static final String CANCEL_PATH = "client/subscription/checkout/cancel";

    private final SubscriptionPlanRepository plans;
    private final SubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final PayPalClient payPal;
    private final EntitlementsService entitlements;
    private final ApplicationEventPublisher events;
    private final AppProperties appProperties;
    private final Clock clock;

    public SubscriptionService(
            SubscriptionPlanRepository plans,
            SubscriptionRepository subscriptions,
            PaymentRepository payments,
            PayPalClient payPal,
            EntitlementsService entitlements,
            ApplicationEventPublisher events,
            AppProperties appProperties,
            Clock clock) {
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.payPal = payPal;
        this.entitlements = entitlements;
        this.events = events;
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

    /** Crea la suscripción pendiente y el pedido en PayPal; devuelve la URL donde el usuario aprueba el pago. */
    @Transactional
    public String checkout(UUID userId, Tier tier) {
        if (tier == Tier.BASIC) {
            throw ApiException.badRequest("basic-is-free", "El plan básico es gratuito: no necesita pago");
        }
        Instant now = clock.instant();
        Tier current = entitlements
                .grantingSubscription(userId, now)
                .map(Subscription::getTier)
                .orElse(Tier.BASIC);
        if (current.includes(tier)) {
            throw ApiException.conflict("already-subscribed", "Ya tienes acceso a ese plan o a uno superior")
                    .with("currentTier", current);
        }
        SubscriptionPlan plan = plans.findByTierAndActiveTrue(tier)
                .orElseThrow(() -> ApiException.notFound("plan-not-found", "Ese plan no está disponible"));

        // Un checkout anterior que no se terminó deja de valer.
        subscriptions
                .findByUserIdAndStatusIn(userId, List.of(SubscriptionStatus.PENDING_PAYMENT))
                .forEach(this::abandon);

        Subscription subscription = subscriptions.save(Subscription.pending(userId, plan));
        CreatedOrder order = payPal.createOrder(
                subscription.requireId().toString(),
                "Healthy Bites " + plan.getName() + " (" + plan.getPeriodDays() + " días)",
                plan.getPrice(),
                appProperties.frontend(RETURN_PATH),
                appProperties.frontend(CANCEL_PATH));
        payments.save(Payment.created(subscription.requireId(), userId, order.id(), plan.getPrice()));
        return order.approvalUrl();
    }

    /** Captura el pedido y activa la suscripción. Repetirlo (recarga, doble clic) no cobra ni activa dos veces. */
    @Transactional
    public Current capture(UUID userId, String orderId) {
        Payment payment = payments.findByOrderIdForUpdate(orderId)
                .filter(candidate -> candidate.getUserId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("order-not-found", "Pedido no encontrado"));
        if (payment.isCaptured()) {
            return current(userId);
        }
        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw ApiException.conflict("order-closed", "Este pedido ya no se puede pagar. Empieza de nuevo.");
        }
        Subscription subscription =
                subscriptions.findById(payment.getSubscriptionId()).orElseThrow();

        CapturedOrder captured = payPal.captureOrder(orderId);
        if (!captured.isCompleted()) {
            payment.fail();
            throw ApiException.conflict("payment-not-completed", "PayPal no completó el pago. No se ha cobrado nada.");
        }
        verify(orderId, captured, subscription, payment);

        Instant now = clock.instant();
        payment.capture(Objects.requireNonNull(captured.captureId()), now);
        subscriptions.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).ifPresent(previous -> {
            previous.cancel(now);
            subscriptions.saveAndFlush(previous); // libera el índice "una ACTIVE por usuario"
        });
        SubscriptionPlan plan = plans.findById(subscription.getPlanId()).orElseThrow();
        subscription.activate(now, plan.period());
        events.publishEvent(new SubscriptionActivated(
                subscription.requireId(), userId, subscription.getTier(), payment.requireId()));
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

    private void abandon(Subscription pending) {
        pending.expire();
        payments.findBySubscriptionIdAndStatus(pending.requireId(), PaymentStatus.CREATED)
                .forEach(Payment::cancel);
    }

    /** Lo cobrado tiene que ser exactamente lo del pedido, en su moneda y para esa suscripción. */
    private static void verify(String orderId, CapturedOrder captured, Subscription subscription, Payment payment) {
        boolean matches = subscription.requireId().toString().equals(captured.referenceId())
                && captured.captureId() != null
                && payment.getAmount().equals(captured.captured());
        if (!matches) {
            // No debería pasar nunca (el importe lo fija el servidor). Si pasa, hay que revisar el cobro a mano.
            log.error(
                    "Captura de PayPal {} que no cuadra: referencia {}, cobrado {}, esperado {} para la suscripción {}",
                    orderId,
                    captured.referenceId(),
                    captured.captured(),
                    payment.getAmount(),
                    subscription.getId());
            payment.fail();
            throw ApiException.badGateway(
                    "payment-mismatch", "El pago no coincide con la suscripción. Contacta con soporte.");
        }
    }
}
