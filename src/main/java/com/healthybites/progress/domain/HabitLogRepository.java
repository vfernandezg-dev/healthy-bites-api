package com.healthybites.progress.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitLogRepository extends JpaRepository<HabitLog, UUID> {

    List<HabitLog> findByClientIdAndLoggedOnBetweenOrderByLoggedOnDesc(UUID clientId, LocalDate from, LocalDate to);

    Optional<HabitLog> findByIdAndClientId(UUID id, UUID clientId);

    boolean existsByClientIdAndLoggedOn(UUID clientId, LocalDate loggedOn);
}
