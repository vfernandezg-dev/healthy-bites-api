package com.healthybites.progress.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Registro de hábitos de un día: cuatro notas de 0 a 10 (uno por día y cliente). La fecha no cambia. */
@Entity
@Table(name = "habit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HabitLog extends BaseEntity {

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "logged_on", nullable = false)
    private LocalDate loggedOn;

    @Column(nullable = false)
    private int hydration;

    @Column(nullable = false)
    private int nutrition;

    @Column(nullable = false)
    private int exercise;

    @Column(nullable = false)
    private int sleep;

    @Column(length = 500)
    private @Nullable String notes;

    public record Scores(
            int hydration,
            int nutrition,
            int exercise,
            int sleep,
            @Nullable String notes) {}

    public static HabitLog of(UUID clientId, LocalDate loggedOn, Scores scores) {
        HabitLog log = new HabitLog();
        log.clientId = clientId;
        log.loggedOn = loggedOn;
        log.update(scores);
        return log;
    }

    public void update(Scores scores) {
        this.hydration = scores.hydration();
        this.nutrition = scores.nutrition();
        this.exercise = scores.exercise();
        this.sleep = scores.sleep();
        this.notes = scores.notes() == null || scores.notes().isBlank()
                ? null
                : scores.notes().trim();
    }

    /** Media de las cuatro notas, con un decimal. */
    public double average() {
        return Math.round((hydration + nutrition + exercise + sleep) / 4.0 * 10) / 10.0;
    }
}
