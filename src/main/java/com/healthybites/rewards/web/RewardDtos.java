package com.healthybites.rewards.web;

import com.healthybites.rewards.app.RewardService.RewardView;
import com.healthybites.rewards.app.RewardService.TargetType;
import com.healthybites.rewards.domain.Reward;
import com.healthybites.rewards.domain.RewardStreak;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class RewardDtos {

    private RewardDtos() {}

    record TargetResponse(TargetType type, UUID id, String title) {}

    record StreakResponse(
            UUID id,
            int currentDays,
            @Nullable LocalDate lastCountedOn,
            boolean completed,
            @Nullable Instant completedAt) {

        static @Nullable StreakResponse of(@Nullable RewardStreak streak) {
            return streak == null
                    ? null
                    : new StreakResponse(
                            streak.requireId(),
                            streak.getCurrentDays(),
                            streak.getLastCountedOn(),
                            streak.isCompleted(),
                            streak.getCompletedAt());
        }
    }

    /** Una recompensa, a qué da acceso y (para el cliente) su racha hacia ella. */
    record RewardResponse(
            UUID id,
            String title,
            String description,
            int requiredStreakDays,
            boolean active,
            TargetResponse target,
            @Nullable StreakResponse streak) {

        static RewardResponse of(RewardView view) {
            Reward reward = view.reward();
            return new RewardResponse(
                    reward.requireId(),
                    reward.getTitle(),
                    reward.getDescription(),
                    reward.getRequiredStreakDays(),
                    reward.isActive(),
                    new TargetResponse(
                            view.target().type(),
                            view.target().id(),
                            view.target().title()),
                    StreakResponse.of(view.streak()));
        }
    }

    record StartStreakRequest(
            @NotNull(message = "Elige una recompensa") UUID rewardId) {}

    /** Exactamente uno de los dos premios: un contenido o un plan propio y publicado. */
    record RewardRequest(
            @NotBlank(message = "El título es obligatorio") @Size(max = 120, message = "Máximo 120 caracteres") String title,

            @NotBlank(message = "La descripción es obligatoria") @Size(max = 500, message = "Máximo 500 caracteres") String description,

            @Min(value = 1, message = "Al menos 1 día") @Max(value = 365, message = "Como mucho 365 días") int requiredStreakDays,

            @Nullable UUID contentItemId,
            @Nullable UUID mealPlanId) {

        Reward.Data toData() {
            return new Reward.Data(title, description, requiredStreakDays, contentItemId, mealPlanId);
        }
    }
}
