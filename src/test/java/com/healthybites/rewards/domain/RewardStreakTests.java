package com.healthybites.rewards.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthybites.rewards.domain.RewardStreak.Outcome;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Reglas de la racha: un día por fecha, los huecos reinician y se completa al llegar a los días requeridos. */
class RewardStreakTests {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);
    private static final Instant NOW = Instant.parse("2026-11-12T10:00:00Z");

    private final RewardStreak streak = RewardStreak.start(UUID.randomUUID(), UUID.randomUUID());

    @Test
    void consecutiveDaysAddUpAndTheSameDayCountsOnce() {
        assertThat(streak.count(MONDAY, 5, NOW)).isEqualTo(Outcome.ADVANCED);
        assertThat(streak.count(MONDAY, 5, NOW)).isEqualTo(Outcome.UNCHANGED);
        assertThat(streak.count(MONDAY.plusDays(1), 5, NOW)).isEqualTo(Outcome.ADVANCED);

        assertThat(streak.getCurrentDays()).isEqualTo(2);
        assertThat(streak.getLastCountedOn()).isEqualTo(MONDAY.plusDays(1));
    }

    @Test
    void aGapStartsOverAndOlderDaysAreIgnored() {
        streak.count(MONDAY, 5, NOW);
        streak.count(MONDAY.plusDays(1), 5, NOW);

        assertThat(streak.count(MONDAY.plusDays(3), 5, NOW)).isEqualTo(Outcome.ADVANCED);
        assertThat(streak.getCurrentDays()).isEqualTo(1);
        assertThat(streak.count(MONDAY.plusDays(2), 5, NOW)).isEqualTo(Outcome.UNCHANGED);
    }

    @Test
    void itCompletesOnceAndThenStops() {
        streak.count(MONDAY, 2, NOW);

        assertThat(streak.count(MONDAY.plusDays(1), 2, NOW)).isEqualTo(Outcome.COMPLETED);
        assertThat(streak.isCompleted()).isTrue();
        assertThat(streak.count(MONDAY.plusDays(2), 2, NOW)).isEqualTo(Outcome.UNCHANGED);
    }
}
