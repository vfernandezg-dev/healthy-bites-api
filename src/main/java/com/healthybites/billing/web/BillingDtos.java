package com.healthybites.billing.web;

import com.healthybites.billing.Tier;
import com.healthybites.billing.app.BillingAdminService;
import com.healthybites.billing.app.SubscriptionService.Current;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentStatus;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionPlan;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.identity.UserAccount;
import com.healthybites.shared.domain.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class BillingDtos {

    private BillingDtos() {}

    record PlanResponse(UUID id, Tier tier, String name, String description, Money price, int periodDays) {

        static PlanResponse of(SubscriptionPlan plan) {
            return new PlanResponse(
                    plan.requireId(),
                    plan.getTier(),
                    plan.getName(),
                    plan.getDescription(),
                    plan.getPrice(),
                    plan.getPeriodDays());
        }
    }

    /** El nivel vigente; sin suscripción, BASIC y el resto vacío. */
    record CurrentSubscriptionResponse(
            Tier tier,
            @Nullable UUID subscriptionId,
            @Nullable SubscriptionStatus status,
            @Nullable Instant currentPeriodEnd,
            boolean renews) {

        static CurrentSubscriptionResponse of(Current current) {
            Subscription subscription = current.subscription();
            return subscription == null
                    ? new CurrentSubscriptionResponse(current.tier(), null, null, null, false)
                    : new CurrentSubscriptionResponse(
                            current.tier(),
                            subscription.getId(),
                            subscription.getStatus(),
                            subscription.getCurrentPeriodEnd(),
                            subscription.getStatus() == SubscriptionStatus.ACTIVE);
        }
    }

    record CheckoutRequest(
            @NotNull(message = "Elige un plan") Tier tier) {}

    record CheckoutResponse(String approvalUrl) {}

    record CaptureRequest(
            @NotBlank(message = "Falta el pedido") @Size(max = 64) String orderId) {}

    record PaymentResponse(
            UUID id,
            UUID subscriptionId,
            Money amount,
            PaymentStatus status,
            @Nullable Instant createdAt,
            @Nullable Instant capturedAt) {

        static PaymentResponse of(Payment payment) {
            return new PaymentResponse(
                    payment.requireId(),
                    payment.getSubscriptionId(),
                    payment.getAmount(),
                    payment.getStatus(),
                    payment.getCreatedAt(),
                    payment.getCapturedAt());
        }
    }

    /** Titular de una suscripción o un pago; {@code null} si la cuenta ya no existe. */
    record CustomerResponse(UUID id, String name, String email) {

        static @Nullable CustomerResponse of(@Nullable UserAccount account) {
            return account == null ? null : new CustomerResponse(account.id(), account.fullName(), account.email());
        }
    }

    record AdminSubscriptionResponse(
            UUID id,
            UUID userId,
            @Nullable CustomerResponse customer,
            Tier tier,
            SubscriptionStatus status,
            @Nullable Instant currentPeriodStart,
            @Nullable Instant currentPeriodEnd,
            @Nullable Instant canceledAt,
            @Nullable Instant createdAt) {

        static AdminSubscriptionResponse of(Subscription subscription, Map<UUID, UserAccount> people) {
            return new AdminSubscriptionResponse(
                    subscription.requireId(),
                    subscription.getUserId(),
                    CustomerResponse.of(people.get(subscription.getUserId())),
                    subscription.getTier(),
                    subscription.getStatus(),
                    subscription.getCurrentPeriodStart(),
                    subscription.getCurrentPeriodEnd(),
                    subscription.getCanceledAt(),
                    subscription.getCreatedAt());
        }
    }

    record AdminPaymentResponse(
            UUID id,
            UUID userId,
            @Nullable CustomerResponse customer,
            UUID subscriptionId,
            String providerOrderId,
            Money amount,
            PaymentStatus status,
            @Nullable Instant createdAt,
            @Nullable Instant capturedAt) {

        static AdminPaymentResponse of(Payment payment, Map<UUID, UserAccount> people) {
            return new AdminPaymentResponse(
                    payment.requireId(),
                    payment.getUserId(),
                    CustomerResponse.of(people.get(payment.getUserId())),
                    payment.getSubscriptionId(),
                    payment.getProviderOrderId(),
                    payment.getAmount(),
                    payment.getStatus(),
                    payment.getCreatedAt(),
                    payment.getCapturedAt());
        }
    }

    record BillingSummaryResponse(
            Map<Tier, Long> activeByTier,
            List<Money> monthlyRecurringRevenue,
            List<Money> revenueLast30Days,
            long paymentsLast30Days) {

        static BillingSummaryResponse of(BillingAdminService.Summary summary) {
            return new BillingSummaryResponse(
                    summary.activeByTier(),
                    summary.monthlyRecurringRevenue(),
                    summary.revenueLast30Days(),
                    summary.paymentsLast30Days());
        }
    }
}
