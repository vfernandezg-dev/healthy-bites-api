package com.healthybites.progress.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightEntryRepository extends JpaRepository<WeightEntry, UUID> {

    List<WeightEntry> findByGoalIdAndMeasuredOnBetweenOrderByMeasuredOnAsc(UUID goalId, LocalDate from, LocalDate to);

    List<WeightEntry> findByGoalIdOrderByMeasuredOnAsc(UUID goalId);

    Optional<WeightEntry> findFirstByGoalIdOrderByMeasuredOnDesc(UUID goalId);

    Optional<WeightEntry> findByIdAndGoalId(UUID id, UUID goalId);

    boolean existsByGoalIdAndMeasuredOn(UUID goalId, LocalDate measuredOn);
}
