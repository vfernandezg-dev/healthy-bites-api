package com.healthybites.billing.app;

import com.healthybites.billing.Tier;
import com.healthybites.billing.app.PaymentGateway.CapturedOrder;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentRepository;
import com.healthybites.billing.domain.PaymentStatus;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionPlan;
import com.healthybites.billing.domain.SubscriptionPlanRepository;
import com.healthybites.billing.domain.SubscriptionRepository;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.billing.events.SubscriptionActivated;
import com.healthybites.shared.domain.Money;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Los pasos del checkout que tocan la base de datos, cada uno en su propia transacción corta.
 * {@link SubscriptionService} llama a la pasarela de pago entre ellos: mientras se espera a PayPal no queda ninguna
 * conexión ni bloqueo retenidos.
 *
 * <p>Un rechazo que cambia el estado del pago (FAILED) no se lanza dentro de la transacción, porque el rollback lo
 * desharía: se devuelve como {@link CaptureOutcome.Rejected} y el servicio lo lanza después del commit.
 */
@Slf4j
@Component
class PaymentLedger {

    private final SubscriptionPlanRepository plans;
    private final SubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final EntitlementsService entitlements;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    PaymentLedger(
            SubscriptionPlanRepository plans,
            SubscriptionRepository subscriptions,
            PaymentRepository payments,
            EntitlementsService entitlements,
            ApplicationEventPublisher events,
            Clock clock) {
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.entitlements = entitlements;
        this.events = events;
        this.clock = clock;
    }

    /** Suscripción pendiente recién creada y lo que hay que pedir a la pasarela para pagarla. */
    record OpenCheckout(UUID subscriptionId, String description, Money price) {}

    /** Si la captura todavía tiene que pedirse a la pasarela. */
    enum Readiness {
        READY,
        ALREADY_CAPTURED
    }

    /** Resultado de aplicar la respuesta de la pasarela. */
    sealed interface CaptureOutcome {

        /** El pago quedó capturado y la suscripción activa (ahora o en una petición anterior). */
        record Captured() implements CaptureOutcome {}

        /** El pago no se aceptó; su nuevo estado ya está guardado. */
        record Rejected(ApiException error) implements CaptureOutcome {}
    }

    /** Valida el plan pedido, cierra el checkout anterior sin terminar y crea la suscripción pendiente de pago. */
    @Transactional
    public OpenCheckout open(UUID userId, Tier tier) {
        if (tier == Tier.BASIC) {
            throw ApiException.badRequest("basic-is-free", "El plan básico es gratuito: no necesita pago");
        }
        Tier current = entitlements
                .grantingSubscription(userId, clock.instant())
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
        return new OpenCheckout(
                subscription.requireId(),
                "Healthy Bites " + plan.getName() + " (" + plan.getPeriodDays() + " días)",
                plan.getPrice());
    }

    /** Registra el pedido que creó la pasarela para la suscripción pendiente. */
    @Transactional
    public void recordOrder(OpenCheckout checkout, UUID userId, String orderId) {
        payments.save(Payment.created(checkout.subscriptionId(), userId, orderId, checkout.price()));
    }

    /** Comprueba, sin bloquear, que el pedido es del usuario y sigue abierto. */
    @Transactional(readOnly = true)
    public Readiness prepareCapture(UUID userId, String orderId) {
        Payment payment = owned(payments.findByProviderOrderId(orderId), userId);
        if (payment.isCaptured()) {
            return Readiness.ALREADY_CAPTURED;
        }
        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw orderClosed();
        }
        return Readiness.READY;
    }

    /**
     * Aplica la respuesta de la pasarela con el pago bloqueado: dos capturas simultáneas del mismo pedido (doble
     * clic, dos pestañas) se aplican una tras otra y la segunda lo ve ya capturado.
     */
    @Transactional
    public CaptureOutcome applyCapture(UUID userId, String orderId, CapturedOrder captured) {
        Payment payment = owned(payments.findByOrderIdForUpdate(orderId), userId);
        if (payment.isCaptured()) {
            return new CaptureOutcome.Captured();
        }
        if (payment.getStatus() != PaymentStatus.CREATED) {
            if (captured.isCompleted()) {
                // El pedido se cerró (checkout abandonado) mientras PayPal cobraba: hay que devolver el dinero a mano.
                log.error(
                        "PayPal capturó el pedido {} ({}) pero el pago ya estaba {}: revisar y reembolsar",
                        orderId,
                        captured.captureId(),
                        payment.getStatus());
            }
            return new CaptureOutcome.Rejected(orderClosed());
        }
        if (!captured.isCompleted()) {
            payment.fail();
            return new CaptureOutcome.Rejected(ApiException.conflict(
                    "payment-not-completed", "PayPal no completó el pago. No se ha cobrado nada."));
        }
        Subscription subscription =
                subscriptions.findById(payment.getSubscriptionId()).orElseThrow();
        if (!matches(captured, subscription, payment)) {
            // No debería pasar nunca (el importe lo fija el servidor). Si pasa, hay que revisar el cobro a mano.
            log.error(
                    "Captura de PayPal {} que no cuadra: referencia {}, cobrado {}, esperado {} para la suscripción {}",
                    orderId,
                    captured.referenceId(),
                    captured.captured(),
                    payment.getAmount(),
                    subscription.getId());
            payment.fail();
            return new CaptureOutcome.Rejected(ApiException.badGateway(
                    "payment-mismatch", "El pago no coincide con la suscripción. Contacta con soporte."));
        }

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
        return new CaptureOutcome.Captured();
    }

    private void abandon(Subscription pending) {
        pending.expire();
        payments.findBySubscriptionIdAndStatus(pending.requireId(), PaymentStatus.CREATED)
                .forEach(Payment::cancel);
    }

    /** Un pedido ajeno responde igual que uno inexistente. */
    private static Payment owned(Optional<Payment> payment, UUID userId) {
        return payment.filter(candidate -> candidate.getUserId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("order-not-found", "Pedido no encontrado"));
    }

    /** Lo cobrado tiene que ser exactamente lo del pedido, en su moneda y para esa suscripción. */
    private static boolean matches(CapturedOrder captured, Subscription subscription, Payment payment) {
        return subscription.requireId().toString().equals(captured.referenceId())
                && captured.captureId() != null
                && payment.getAmount().equals(captured.captured());
    }

    private static ApiException orderClosed() {
        return ApiException.conflict("order-closed", "Este pedido ya no se puede pagar. Empieza de nuevo.");
    }
}
