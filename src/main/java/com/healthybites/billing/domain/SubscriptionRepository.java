package com.healthybites.billing.domain;

import com.healthybites.billing.Tier;
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

    /** Suscripciones que dan acceso ahora: activas o canceladas con período vigente, la de nivel más alto primero. */
    @Query("""
            select s from Subscription s
            where s.userId = :userId and s.status in (com.healthybites.billing.domain.SubscriptionStatus.ACTIVE, com.healthybites.billing.domain.SubscriptionStatus.CANCELED) and s.currentPeriodEnd > :now
            order by s.tier desc, s.currentPeriodEnd desc""")
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

    interface TierCount {
        Tier getTier();

        long getTotal();
    }
}
