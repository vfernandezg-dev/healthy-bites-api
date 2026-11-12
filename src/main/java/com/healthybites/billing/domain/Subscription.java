package com.healthybites.billing.domain;

import com.healthybites.billing.Tier;
import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Suscripción de un usuario a un plan de pago.
 *
 * <p>PENDING_PAYMENT → ACTIVE al capturar el pago → CANCELED si el usuario la cancela (mantiene el acceso hasta el fin
 * del período) → EXPIRED al terminar el período. Una PENDING_PAYMENT abandonada también acaba en EXPIRED.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "current_period_start")
    private @Nullable Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private @Nullable Instant currentPeriodEnd;

    @Column(name = "canceled_at")
    private @Nullable Instant canceledAt;

    public static Subscription pending(UUID userId, SubscriptionPlan plan) {
        Subscription subscription = new Subscription();
        subscription.userId = userId;
        subscription.planId = plan.requireId();
        subscription.tier = plan.getTier();
        subscription.status = SubscriptionStatus.PENDING_PAYMENT;
        return subscription;
    }

    public void activate(Instant now, Duration period) {
        if (status != SubscriptionStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Solo se activa una suscripción pendiente de pago");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = now;
        this.currentPeriodEnd = now.plus(period);
    }

    /** Cancelada: deja de renovarse, pero el acceso sigue hasta el fin del período pagado. */
    public void cancel(Instant now) {
        if (status == SubscriptionStatus.ACTIVE) {
            this.status = SubscriptionStatus.CANCELED;
            this.canceledAt = now;
        }
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /** Da acceso en ese momento: activa, o cancelada con período pagado vigente. */
    public boolean grantsAccessAt(Instant now) {
        return (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED)
                && currentPeriodEnd != null
                && now.isBefore(currentPeriodEnd);
    }
}
