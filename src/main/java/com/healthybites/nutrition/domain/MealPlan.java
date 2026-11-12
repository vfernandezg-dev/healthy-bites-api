package com.healthybites.nutrition.domain;

import com.healthybites.billing.Tier;
import com.healthybites.nutrition.domain.NutritionEnums.MealCategory;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.shared.domain.PublishableEntity;
import com.healthybites.shared.web.ApiException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.jspecify.annotations.Nullable;

/**
 * Plan de alimentación (agregado con sus comidas). Se crea en borrador, se publica cuando tiene al menos una comida
 * y se archiva para retirarlo del catálogo sin perder las inscripciones.
 */
@Entity
@Table(name = "meal_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealPlan extends PublishableEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealPlanGoal goal;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_tier", nullable = false, length = 10)
    private Tier requiredTier;

    // Por lotes: al pintar un catálogo, las comidas de toda la página se cargan con una o dos consultas.
    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber asc, position asc")
    @BatchSize(size = 50)
    private List<Meal> meals = new ArrayList<>();

    public static MealPlan draft(
            UUID authorId, String title, String description, MealPlanGoal goal, int durationDays, Tier requiredTier) {
        MealPlan plan = new MealPlan();
        plan.authorId = authorId;
        plan.update(title, description, goal, durationDays, requiredTier);
        return plan;
    }

    public void update(String title, String description, MealPlanGoal goal, int durationDays, Tier requiredTier) {
        if (meals.stream().anyMatch(meal -> meal.getDayNumber() > durationDays)) {
            throw ApiException.conflict(
                    "meals-outside-duration", "Hay comidas en días posteriores a la nueva duración: muévelas antes");
        }
        this.title = title.trim();
        this.description = description.trim();
        this.goal = goal;
        this.durationDays = durationDays;
        this.requiredTier = requiredTier;
    }

    public List<Meal> getMeals() {
        return Collections.unmodifiableList(meals);
    }

    public Meal addMeal(
            int dayNumber,
            int position,
            MealCategory category,
            String name,
            @Nullable String description,
            int calories) {
        checkDay(dayNumber);
        checkSlotFree(dayNumber, position, null);
        Meal meal = Meal.create(this, dayNumber, position, category, name, description, calories);
        meals.add(meal);
        return meal;
    }

    public Meal updateMeal(
            UUID mealId,
            int dayNumber,
            int position,
            MealCategory category,
            String name,
            @Nullable String description,
            int calories) {
        Meal meal = meal(mealId);
        checkDay(dayNumber);
        checkSlotFree(dayNumber, position, mealId);
        meal.update(dayNumber, position, category, name, description, calories);
        return meal;
    }

    public void removeMeal(UUID mealId) {
        meals.remove(meal(mealId));
    }

    @Override
    public void publish(Instant now) {
        if (meals.isEmpty()) {
            throw ApiException.conflict("plan-without-meals", "Añade al menos una comida antes de publicar el plan");
        }
        super.publish(now);
    }

    /** Calorías medias por día, sobre los días que tienen alguna comida. */
    public int averageDailyCalories() {
        return (int) Math.round(meals.stream()
                .collect(Collectors.groupingBy(Meal::getDayNumber, Collectors.summingInt(Meal::getCalories)))
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));
    }

    private Meal meal(UUID mealId) {
        return meals.stream()
                .filter(meal -> mealId.equals(meal.getId()))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("meal-not-found", "La comida no existe en este plan"));
    }

    private void checkDay(int dayNumber) {
        if (dayNumber < 1 || dayNumber > durationDays) {
            throw ApiException.unprocessable(
                    "day-outside-plan", "El día debe estar entre 1 y " + durationDays + " (duración del plan)");
        }
    }

    private void checkSlotFree(int dayNumber, int position, @Nullable UUID ignoring) {
        boolean taken = meals.stream()
                .anyMatch(meal -> meal.getDayNumber() == dayNumber
                        && meal.getPosition() == position
                        && (ignoring == null || !ignoring.equals(meal.getId())));
        if (taken) {
            throw ApiException.conflict("meal-slot-taken", "Ya hay una comida en ese día y posición");
        }
    }
}
