package com.healthybites.progress.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    List<Goal> findByClientIdAndStatusOrderByCreatedAtDesc(UUID clientId, Goal.Status status);

    Optional<Goal> findByIdAndClientId(UUID id, UUID clientId);

    boolean existsByClientIdAndStatus(UUID clientId, Goal.Status status);
}
