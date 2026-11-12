package com.healthybites.nutrition.domain;

/** Valores cerrados del módulo. El web-app los muestra en español. */
public final class NutritionEnums {

    private NutritionEnums() {}

    public enum MealPlanGoal {
        WEIGHT_LOSS,
        MAINTENANCE,
        MUSCLE_GAIN
    }

    public enum MealCategory {
        BREAKFAST,
        LUNCH,
        DINNER,
        SNACK
    }

    public enum EnrollmentSource {
        SELF,
        REWARD
    }
}
