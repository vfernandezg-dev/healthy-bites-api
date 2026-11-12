package com.healthybites.billing.domain;

import com.healthybites.billing.Tier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {

    /**
     * Suscripciones que dan acceso ahora: activas o canceladas con período vigente. Sin orden: el nivel es un enum
     * guardado como texto, así que la de nivel más alto se elige en Java ({@code EntitlementsService}).
     */
    @Query("""
            select s from Subscription s
            where s.userId = :userId and s.status in (com.healthybites.billing.domain.SubscriptionStatus.ACTIVE, com.healthybites.billing.domain.SubscriptionStatus.CANCELED) and s.currentPeriodEnd > :now""")
    List<Subscription> findGrantingAccess(@Param("userId") UUID userId, @Param("now") Instant now);

    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    List<Subscription> findByUserIdAndStatusIn(UUID userId, List<SubscriptionStatus> statuses);

    /** Activas o canceladas cuyo período ya terminó. */
    @Query("""
            select s from Subscription s
            where s.status in (com.healthybites.billing.domain.SubscriptionStatus.ACTIVE, com.healthybites.billing.domain.SubscriptionStatus.CANCELED) and s.currentPeriodEnd <= :now""")
    List<Subscription> findEndedPeriods(@Param("now") Instant now);

    /** Pendientes de pago desde antes de {@code before}: checkouts abandonados. */
    @Query(
            "select s from Subscription s where s.status = com.healthybites.billing.domain.SubscriptionStatus.PENDING_PAYMENT and s.createdAt < :before")
    List<Subscription> findAbandoned(@Param("before") Instant before);

    @Query("""
            select s.tier as tier, count(s) as total from Subscription s
            where s.status in (com.healthybites.billing.domain.SubscriptionStatus.ACTIVE, com.healthybites.billing.domain.SubscriptionStatus.CANCELED) and s.currentPeriodEnd > :now group by s.tier""")
    List<TierCount> countGrantingAccessByTier(@Param("now") Instant now);

    /**
     * Ingreso mensual recurrente por moneda: el precio mensual (precio × 30 / días del período) de las suscripciones
     * ACTIVE vigentes. Las canceladas siguen dando acceso, pero ya no se renuevan.
     */
    @Query("""
            select p.price.currency as currency, sum(p.price.amount * 30 / p.periodDays) as total
            from Subscription s join SubscriptionPlan p on p.id = s.planId
            where s.status = com.healthybites.billing.domain.SubscriptionStatus.ACTIVE and s.currentPeriodEnd > :now
            group by p.price.currency order by p.price.currency""")
    List<MonthlyRevenue> monthlyRecurringRevenue(@Param("now") Instant now);

    interface MonthlyRevenue {
        String getCurrency();

        BigDecimal getTotal();
    }

    interface TierCount {
        Tier getTier();

        long getTotal();
    }
}
