package com.healthybites.billing.app;

import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionRepository;
import com.healthybites.billing.events.SubscriptionExpired;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cada hora: las suscripciones cuyo período pagado terminó pasan a EXPIRED (y se avisa con un evento), y los
 * checkouts abandonados hace más de 24 horas se cierran.
 */
@Slf4j
@Component
public class SubscriptionLifecycleJob {

    private static final Duration ABANDONED_AFTER = Duration.ofHours(24);

    private final SubscriptionRepository subscriptions;
    private final PaymentLedger ledger;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public SubscriptionLifecycleJob(
            SubscriptionRepository subscriptions, PaymentLedger ledger, ApplicationEventPublisher events, Clock clock) {
        this.subscriptions = subscriptions;
        this.ledger = ledger;
        this.events = events;
        this.clock = clock;
    }

    @Scheduled(cron = "0 5 * * * *")
    @SchedulerLock(name = "subscription-lifecycle", lockAtMostFor = "PT10M")
    @Transactional
    public void run() {
        Instant now = clock.instant();
        int expired = 0;
        for (Subscription subscription : subscriptions.findEndedPeriods(now)) {
            subscription.expire();
            events.publishEvent(new SubscriptionExpired(
                    subscription.requireId(), subscription.getUserId(), subscription.getTier()));
            expired++;
        }
        List<Subscription> pending = subscriptions.findAbandoned(now.minus(ABANDONED_AFTER));
        ledger.abandon(pending);
        int abandoned = pending.size();
        if (expired + abandoned > 0) {
            log.info("Suscripciones vencidas: {}; checkouts abandonados cerrados: {}", expired, abandoned);
        }
    }
}
