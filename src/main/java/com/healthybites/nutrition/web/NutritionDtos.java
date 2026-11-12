package com.healthybites.nutrition.web;

import com.healthybites.billing.Tier;
import com.healthybites.identity.UserAccount;
import com.healthybites.nutrition.app.MealPlanService.PlanCard;
import com.healthybites.nutrition.app.NutritionistPlanService;
import com.healthybites.nutrition.domain.Meal;
import com.healthybites.nutrition.domain.MealPlan;
import com.healthybites.nutrition.domain.MealPlanEnrollment;
import com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource;
import com.healthybites.nutrition.domain.NutritionEnums.MealCategory;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanStatus;
import com.healthybites.profiles.ClientProfiles.ClientProfileView;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class NutritionDtos {

    private NutritionDtos() {}

    record AuthorResponse(UUID id, String name) {

        static @Nullable AuthorResponse of(@Nullable UserAccount account) {
            return account == null ? null : new AuthorResponse(account.id(), account.fullName());
        }
    }

    /** Ficha del catálogo: sin comidas. {@code locked} indica si el lector puede abrirlo. */
    record MealPlanSummaryResponse(
            UUID id,
            String title,
            String description,
            MealPlanGoal goal,
            int durationDays,
            Tier requiredTier,
            boolean locked,
            int averageDailyCalories,
            MealPlanStatus status,
            @Nullable AuthorResponse author,
            @Nullable Instant publishedAt) {

        static MealPlanSummaryResponse of(PlanCard card) {
            MealPlan plan = card.plan();
            return new MealPlanSummaryResponse(
                    plan.requireId(),
                    plan.getTitle(),
                    plan.getDescription(),
                    plan.getGoal(),
                    plan.getDurationDays(),
                    plan.getRequiredTier(),
                    card.locked(),
                    card.averageDailyCalories(),
                    plan.getStatus(),
                    AuthorResponse.of(card.author()),
                    plan.getPublishedAt());
        }
    }

    record MealResponse(
            UUID id,
            int dayNumber,
            int position,
            MealCategory category,
            String name,
            @Nullable String description,
            int calories) {

        static MealResponse of(Meal meal) {
            return new MealResponse(
                    meal.requireId(),
                    meal.getDayNumber(),
                    meal.getPosition(),
                    meal.getCategory(),
                    meal.getName(),
                    meal.getDescription(),
                    meal.getCalories());
        }
    }

    record MealPlanDetailResponse(MealPlanSummaryResponse plan, MealPlanStatus status, List<MealResponse> meals) {

        static MealPlanDetailResponse of(PlanCard card) {
            return new MealPlanDetailResponse(
                    MealPlanSummaryResponse.of(card),
                    card.plan().getStatus(),
                    card.plan().getMeals().stream().map(MealResponse::of).toList());
        }
    }

    record EnrollRequest(@NotNull(message = "Elige un plan") UUID mealPlanId) {}

    record EnrollmentResponse(UUID id, EnrollmentSource source, Instant enrolledAt, MealPlanSummaryResponse plan) {

        static EnrollmentResponse of(MealPlanEnrollment enrollment, PlanCard card) {
            return new EnrollmentResponse(
                    Objects.requireNonNull(enrollment.getId()),
                    enrollment.getSource(),
                    enrollment.getEnrolledAt(),
                    MealPlanSummaryResponse.of(card));
        }
    }

    record MealPlanRequest(
            @NotBlank(message = "El título es obligatorio") @Size(max = 120, message = "Máximo 120 caracteres") String title,

            @NotBlank(message = "La descripción es obligatoria") @Size(max = 2000, message = "Máximo 2000 caracteres") String description,

            @NotNull(message = "Elige un objetivo") MealPlanGoal goal,

            @Min(value = 1, message = "Al menos 1 día") @Max(value = 365, message = "Como mucho 365 días") int durationDays,

            @NotNull(message = "Elige el nivel requerido") Tier requiredTier) {

        NutritionistPlanService.PlanData toData() {
            return new NutritionistPlanService.PlanData(title, description, goal, durationDays, requiredTier);
        }
    }

    record MealRequest(
            @Min(value = 1, message = "El día empieza en 1") int dayNumber,

            @Min(value = 0, message = "La posición empieza en 0") @Max(value = 20, message = "Como mucho 20 comidas al día") int position,

            @NotNull(message = "Elige una categoría") MealCategory category,

            @NotBlank(message = "El nombre es obligatorio") @Size(max = 120, message = "Máximo 120 caracteres") String name,

            @Size(max = 1000, message = "Máximo 1000 caracteres") @Nullable String description,

            @Min(value = 0, message = "Las calorías no pueden ser negativas") @Max(value = 10000, message = "Máximo 10000 kcal") int calories) {

        NutritionistPlanService.MealData toData() {
            return new NutritionistPlanService.MealData(dayNumber, position, category, name, description, calories);
        }
    }

    record NutritionistClientResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg,
            List<String> plans,
            Instant since) {

        static NutritionistClientResponse of(NutritionistPlanService.Client client) {
            ClientProfileView profile = client.profile();
            return new NutritionistClientResponse(
                    client.account().id(),
                    client.account().firstName(),
                    client.account().lastName(),
                    client.account().email(),
                    profile == null ? null : profile.heightCm(),
                    profile == null ? null : profile.weightKg(),
                    client.planTitles(),
                    client.since());
        }
    }
}
