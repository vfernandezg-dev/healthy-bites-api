package com.healthybites.billing.domain;

import com.healthybites.billing.Tier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    List<SubscriptionPlan> findByActiveTrueOrderByPriceAmountAsc();

    Optional<SubscriptionPlan> findByTierAndActiveTrue(Tier tier);
}
