package com.healthybites.progress.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Pesaje de un día dentro de una meta (uno por día). */
@Entity
@Table(name = "weight_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeightEntry extends BaseEntity {

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "measured_on", nullable = false)
    private LocalDate measuredOn;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(length = 500)
    private @Nullable String notes;

    public static WeightEntry of(UUID goalId, LocalDate measuredOn, BigDecimal weightKg, @Nullable String notes) {
        WeightEntry entry = new WeightEntry();
        entry.goalId = goalId;
        entry.measuredOn = measuredOn;
        entry.update(weightKg, notes);
        return entry;
    }

    public void update(BigDecimal weightKg, @Nullable String notes) {
        this.weightKg = weightKg;
        this.notes = notes == null || notes.isBlank() ? null : notes.trim();
    }
}
