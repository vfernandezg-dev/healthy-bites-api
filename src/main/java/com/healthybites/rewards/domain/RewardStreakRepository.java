package com.healthybites.rewards.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardStreakRepository extends JpaRepository<RewardStreak, UUID> {

    List<RewardStreak> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    List<RewardStreak> findByClientIdAndCompletedAtIsNull(UUID clientId);

    Optional<RewardStreak> findByRewardIdAndClientId(UUID rewardId, UUID clientId);

    Optional<RewardStreak> findByIdAndClientId(UUID id, UUID clientId);
}
