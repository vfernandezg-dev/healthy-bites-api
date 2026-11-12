package com.healthybites.nutrition.domain;

import com.healthybites.nutrition.domain.NutritionEnums.MealCategory;
import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Comida de un plan: un día y una posición dentro del día (únicos en el plan). Solo se cambia desde el plan. */
@Entity
@Table(name = "meals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @Column(name = "day_number", nullable = false)
    private int dayNumber;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealCategory category;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private @Nullable String description;

    @Column(nullable = false)
    private int calories;

    static Meal create(
            MealPlan plan,
            int dayNumber,
            int position,
            MealCategory category,
            String name,
            @Nullable String description,
            int calories) {
        Meal meal = new Meal();
        meal.mealPlan = plan;
        meal.update(dayNumber, position, category, name, description, calories);
        return meal;
    }

    void update(
            int dayNumber,
            int position,
            MealCategory category,
            String name,
            @Nullable String description,
            int calories) {
        this.dayNumber = dayNumber;
        this.position = position;
        this.category = category;
        this.name = name.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.calories = calories;
    }
}
