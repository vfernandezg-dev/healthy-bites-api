package com.healthybites.rewards.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, UUID> {

    List<Reward> findByActiveTrueOrderByRequiredStreakDaysAsc();

    List<Reward> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    Optional<Reward> findByIdAndAuthorId(UUID id, UUID authorId);
}
