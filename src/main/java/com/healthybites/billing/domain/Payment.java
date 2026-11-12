package com.healthybites.billing.domain;

import com.healthybites.shared.domain.BaseEntity;
import com.healthybites.shared.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Un pago con PayPal: el pedido creado y, si se completa, su captura. */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_order_id", nullable = false, unique = true, length = 64)
    private String providerOrderId;

    @Column(name = "provider_capture_id", length = 64)
    private @Nullable String providerCaptureId;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "captured_at")
    private @Nullable Instant capturedAt;

    public static Payment created(UUID subscriptionId, UUID userId, String providerOrderId, Money amount) {
        Payment payment = new Payment();
        payment.subscriptionId = subscriptionId;
        payment.userId = userId;
        payment.provider = "PAYPAL";
        payment.providerOrderId = providerOrderId;
        payment.amount = amount;
        payment.status = PaymentStatus.CREATED;
        return payment;
    }

    public void capture(String captureId, Instant now) {
        this.status = PaymentStatus.CAPTURED;
        this.providerCaptureId = captureId;
        this.capturedAt = now;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel() {
        if (status == PaymentStatus.CREATED) {
            this.status = PaymentStatus.CANCELED;
        }
    }

    public boolean isCaptured() {
        return status == PaymentStatus.CAPTURED;
    }
}
