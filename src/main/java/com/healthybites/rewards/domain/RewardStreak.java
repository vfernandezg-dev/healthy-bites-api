package com.healthybites.rewards.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Racha de un cliente hacia una recompensa. */
@Entity
@Table(name = "reward_streaks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardStreak extends BaseEntity {

    public enum Outcome {
        /** Nada cambia: ya completada, o un día que ya contó (o anterior al último contado). */
        UNCHANGED,
        ADVANCED,
        COMPLETED
    }

    @Column(name = "reward_id", nullable = false)
    private UUID rewardId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "current_days", nullable = false)
    private int currentDays;

    @Column(name = "last_counted_on")
    private @Nullable LocalDate lastCountedOn;

    @Column(name = "completed_at")
    private @Nullable Instant completedAt;

    public static RewardStreak start(UUID rewardId, UUID clientId) {
        RewardStreak streak = new RewardStreak();
        streak.rewardId = rewardId;
        streak.clientId = clientId;
        return streak;
    }

    /**
     * Cuenta un día con hábitos registrados: suma uno si es el día siguiente al último contado, vuelve a 1 si hubo un
     * hueco y no hace nada si ese día (o uno posterior) ya contó. Al llegar a {@code requiredDays} se completa.
     */
    public Outcome count(LocalDate day, int requiredDays, Instant now) {
        if (completedAt != null || (lastCountedOn != null && !day.isAfter(lastCountedOn))) {
            return Outcome.UNCHANGED;
        }
        boolean consecutive = lastCountedOn != null && day.equals(lastCountedOn.plusDays(1));
        currentDays = consecutive ? currentDays + 1 : 1;
        lastCountedOn = day;
        if (currentDays >= requiredDays) {
            completedAt = now;
            return Outcome.COMPLETED;
        }
        return Outcome.ADVANCED;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }
}
