package com.healthybites.billing.app;

import com.healthybites.billing.Receipts;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentRepository;
import com.healthybites.billing.domain.SubscriptionRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReceiptService implements Receipts {

    private final PaymentRepository payments;
    private final SubscriptionRepository subscriptions;

    public ReceiptService(PaymentRepository payments, SubscriptionRepository subscriptions) {
        this.payments = payments;
        this.subscriptions = subscriptions;
    }

    @Override
    public Optional<Receipt> find(UUID paymentId) {
        return payments.findById(paymentId)
                .filter(Payment::isCaptured)
                .flatMap(payment -> subscriptions
                        .findById(payment.getSubscriptionId())
                        .map(subscription -> new Receipt(
                                payment.getUserId(),
                                subscription.getTier(),
                                payment.getAmount(),
                                Objects.requireNonNull(payment.getCapturedAt()),
                                subscription.getCurrentPeriodEnd(),
                                Objects.requireNonNullElse(
                                        payment.getProviderCaptureId(), payment.getProviderOrderId()))));
    }
}
