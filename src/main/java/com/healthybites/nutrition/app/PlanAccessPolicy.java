package com.healthybites.nutrition.app;

import com.healthybites.billing.Access;
import com.healthybites.billing.Entitlements;
import com.healthybites.nutrition.domain.MealPlan;
import com.healthybites.nutrition.domain.MealPlanEnrollmentRepository;
import com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Quién abre un plan publicado: los planes BASIC, cualquiera; los de pago, quien tenga ese nivel o uno superior o lo
 * haya desbloqueado con una recompensa. Nutricionistas y ADMIN (el equipo) los abren todos.
 */
@Component
class PlanAccessPolicy {

    private final Entitlements entitlements;
    private final MealPlanEnrollmentRepository enrollments;

    PlanAccessPolicy(Entitlements entitlements, MealPlanEnrollmentRepository enrollments) {
        this.entitlements = entitlements;
        this.enrollments = enrollments;
    }

    /** Lo que puede abrir el lector, para decidir sobre muchos planes con dos consultas. */
    Access reader(UUID userId, boolean staff) {
        return staff
                ? Access.forStaff()
                : new Access(false, entitlements.tierOf(userId), enrollments.rewardUnlockedPlanIds(userId));
    }

    static boolean canOpen(Access access, MealPlan plan) {
        return access.canOpen(plan.getRequiredTier(), plan.requireId());
    }

    void requireOpen(MealPlan plan, UUID userId, boolean staff) {
        if (staff) {
            return;
        }
        boolean open = entitlements.tierOf(userId).includes(plan.getRequiredTier())
                || enrollments.existsByMealPlanIdAndClientIdAndSource(
                        plan.requireId(), userId, EnrollmentSource.REWARD);
        if (!open) {
            throw Access.subscriptionRequired(
                    plan.getRequiredTier(),
                    "Este plan requiere una suscripción " + plan.getRequiredTier()
                            + " o desbloquearlo con una recompensa");
        }
    }
}
