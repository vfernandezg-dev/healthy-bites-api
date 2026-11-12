package com.healthybites.rewards.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, UUID> {

    Page<Reward> findByActiveTrue(Pageable pageable);

    Page<Reward> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Reward> findByAuthorIdAndActive(UUID authorId, boolean active, Pageable pageable);

    Optional<Reward> findByIdAndAuthorId(UUID id, UUID authorId);
}
