package com.healthybites.billing.domain;

import com.healthybites.billing.Tier;
import com.healthybites.shared.domain.BaseEntity;
import com.healthybites.shared.domain.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Plan de suscripción. El precio vive en la base de datos: cambiarlo no requiere desplegar. */
@Entity
@Table(name = "subscription_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionPlan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 10)
    private Tier tier;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
    private Money price;

    @Column(name = "period_days", nullable = false)
    private int periodDays;

    @Column(nullable = false)
    private boolean active;

    public Duration period() {
        return Duration.ofDays(periodDays);
    }
}
