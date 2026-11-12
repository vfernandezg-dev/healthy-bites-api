package com.healthybites.progress.web;

import com.healthybites.progress.app.GoalService;
import com.healthybites.progress.domain.Goal;
import com.healthybites.progress.domain.GoalStatusChange;
import com.healthybites.progress.domain.HabitLog;
import com.healthybites.progress.domain.WeightEntry;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class ProgressDtos {

    private ProgressDtos() {}

    record GoalRequest(
            @NotBlank(message = "El título es obligatorio") @Size(max = 120, message = "Máximo 120 caracteres") String title,

            @Size(max = 500, message = "Máximo 500 caracteres") @Nullable String description,

            @NotNull(message = "Indica el peso objetivo") @DecimalMin(value = "20", message = "El peso mínimo es 20 kg") @DecimalMax(value = "400", message = "El peso máximo es 400 kg") @Digits(integer = 3, fraction = 1, message = "Como mucho un decimal") BigDecimal targetWeightKg) {

        GoalService.GoalData toData() {
            return new GoalService.GoalData(title, description, targetWeightKg);
        }
    }

    record GoalResponse(
            UUID id,
            String title,
            @Nullable String description,
            BigDecimal targetWeightKg,
            Goal.Status status,
            LocalDate startedOn,
            @Nullable Instant completedAt,
            @Nullable Instant createdAt) {

        static GoalResponse of(Goal goal) {
            return new GoalResponse(
                    goal.requireId(),
                    goal.getTitle(),
                    goal.getDescription(),
                    goal.getTargetWeightKg(),
                    goal.getStatus(),
                    goal.getStartedOn(),
                    goal.getCompletedAt(),
                    goal.getCreatedAt());
        }
    }

    record StatusChangeResponse(Goal.@Nullable Status from, Goal.Status to, Instant changedAt) {

        static StatusChangeResponse of(GoalStatusChange change) {
            return new StatusChangeResponse(change.getFromStatus(), change.getToStatus(), change.getChangedAt());
        }
    }

    record WeightEntryRequest(
            @NotNull(message = "Indica la fecha") LocalDate measuredOn,

            @NotNull(message = "Indica el peso") @DecimalMin(value = "20", message = "El peso mínimo es 20 kg") @DecimalMax(value = "400", message = "El peso máximo es 400 kg") @Digits(integer = 3, fraction = 1, message = "Como mucho un decimal") BigDecimal weightKg,

            @Size(max = 500, message = "Máximo 500 caracteres") @Nullable String notes) {

        GoalService.WeightData toData() {
            return new GoalService.WeightData(measuredOn, weightKg, notes);
        }
    }

    record WeightEntryResponse(
            UUID id,
            LocalDate measuredOn,
            BigDecimal weightKg,
            @Nullable String notes) {

        static WeightEntryResponse of(WeightEntry entry) {
            return new WeightEntryResponse(
                    entry.requireId(), entry.getMeasuredOn(), entry.getWeightKg(), entry.getNotes());
        }
    }

    record RecommendationResponse(
            BigDecimal heightCm,
            @Nullable BigDecimal currentWeightKg,
            BigDecimal targetWeightKg,
            BigDecimal healthyMinKg,
            BigDecimal healthyMaxKg,
            @Nullable BigDecimal currentBmi,
            BigDecimal targetBmi,
            boolean targetInHealthyRange,
            @Nullable BigDecimal weeklyChangeKg,
            @Nullable Integer estimatedWeeks,
            String message) {

        static RecommendationResponse of(GoalService.Recommendation r) {
            return new RecommendationResponse(
                    r.heightCm(),
                    r.currentWeightKg(),
                    r.targetWeightKg(),
                    r.healthyMinKg(),
                    r.healthyMaxKg(),
                    r.currentBmi(),
                    r.targetBmi(),
                    r.targetInHealthyRange(),
                    r.weeklyChangeKg(),
                    r.estimatedWeeks(),
                    r.message());
        }
    }

    /** Sin fecha, hoy. Solo se aceptan hoy y ayer. */
    record HabitLogRequest(
            @Nullable LocalDate loggedOn,

            @Min(value = 0, message = "De 0 a 10") @Max(value = 10, message = "De 0 a 10") int hydration,

            @Min(value = 0, message = "De 0 a 10") @Max(value = 10, message = "De 0 a 10") int nutrition,

            @Min(value = 0, message = "De 0 a 10") @Max(value = 10, message = "De 0 a 10") int exercise,

            @Min(value = 0, message = "De 0 a 10") @Max(value = 10, message = "De 0 a 10") int sleep,

            @Size(max = 500, message = "Máximo 500 caracteres") @Nullable String notes) {

        HabitLog.Scores toScores() {
            return new HabitLog.Scores(hydration, nutrition, exercise, sleep, notes);
        }
    }

    record HabitLogResponse(
            UUID id,
            LocalDate loggedOn,
            int hydration,
            int nutrition,
            int exercise,
            int sleep,
            double average,
            @Nullable String notes) {

        static HabitLogResponse of(HabitLog log) {
            return new HabitLogResponse(
                    log.requireId(),
                    log.getLoggedOn(),
                    log.getHydration(),
                    log.getNutrition(),
                    log.getExercise(),
                    log.getSleep(),
                    log.average(),
                    log.getNotes());
        }
    }
}
