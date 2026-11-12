package com.healthybites.billing.app;

import com.healthybites.billing.Entitlements;
import com.healthybites.billing.Tier;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EntitlementsService implements Entitlements {

    private final SubscriptionRepository subscriptions;
    private final Clock clock;

    public EntitlementsService(SubscriptionRepository subscriptions, Clock clock) {
        this.subscriptions = subscriptions;
        this.clock = clock;
    }

    @Override
    public Tier tierOf(UUID userId) {
        return grantingSubscription(userId, clock.instant())
                .map(Subscription::getTier)
                .orElse(Tier.BASIC);
    }

    /** La suscripción vigente de nivel más alto (y, a igual nivel, la que termina más tarde). */
    Optional<Subscription> grantingSubscription(UUID userId, Instant now) {
        return subscriptions.findGrantingAccess(userId, now).stream()
                .max(Comparator.comparing(Subscription::getTier)
                        .thenComparing(
                                subscription -> subscription.getCurrentPeriodEnd(),
                                Comparator.nullsFirst(Comparator.naturalOrder())));
    }
}
