package com.healthybites.billing.app;

import com.healthybites.billing.Tier;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentRepository;
import com.healthybites.billing.domain.PaymentStatus;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionRepository;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.shared.domain.Money;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Facturación para el panel de ADMIN: suscripciones, pagos y un resumen de ingresos. */
@Service
@Transactional(readOnly = true)
public class BillingAdminService {

    private final SubscriptionRepository subscriptions;
    private final PaymentRepository payments;
    private final Clock clock;

    public BillingAdminService(SubscriptionRepository subscriptions, PaymentRepository payments, Clock clock) {
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.clock = clock;
    }

    /**
     * @param activeByTier            suscripciones vigentes por nivel
     * @param monthlyRecurringRevenue ingreso mensual recurrente: precio mensual de los planes de las suscripciones
     *                                vigentes (las canceladas, no: ya no se renuevan)
     * @param revenueLast30Days       cobrado en los últimos 30 días
     */
    public record Summary(
            Map<Tier, Long> activeByTier,
            List<Money> monthlyRecurringRevenue,
            List<Money> revenueLast30Days,
            long paymentsLast30Days) {}

    public Page<Subscription> subscriptions(
            @Nullable SubscriptionStatus status, @Nullable Tier tier, Pageable pageable) {
        Specification<Subscription> spec = Specification.unrestricted();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (tier != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tier"), tier));
        }
        return subscriptions.findAll(spec, pageable);
    }

    public Page<Payment> payments(@Nullable PaymentStatus status, Pageable pageable) {
        Specification<Payment> spec = Specification.unrestricted();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return payments.findAll(spec, pageable);
    }

    public Summary summary() {
        Instant now = clock.instant();
        Map<Tier, Long> activeByTier = new EnumMap<>(Tier.class);
        activeByTier.put(Tier.PREMIUM, 0L);
        activeByTier.put(Tier.VIP, 0L);
        subscriptions.countGrantingAccessByTier(now).forEach(row -> activeByTier.put(row.getTier(), row.getTotal()));

        List<PaymentRepository.Revenue> revenue = payments.revenueSince(now.minus(Duration.ofDays(30)));
        return new Summary(
                activeByTier,
                subscriptions.monthlyRecurringRevenue(now).stream()
                        .map(row -> new Money(row.getTotal(), row.getCurrency()))
                        .toList(),
                revenue.stream()
                        .map(r -> new Money(r.getTotal(), r.getCurrency()))
                        .toList(),
                revenue.stream()
                        .mapToLong(PaymentRepository.Revenue::getPayments)
                        .sum());
    }
}
