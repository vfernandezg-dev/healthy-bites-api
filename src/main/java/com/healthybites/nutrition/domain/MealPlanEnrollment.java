package com.healthybites.nutrition.domain;

import com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

/**
 * Un cliente sigue un plan. Si la inscripción viene de una recompensa (REWARD), además le da acceso al plan aunque
 * su suscripción no lo incluya.
 */
@Entity
@Table(name = "meal_plan_enrollments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealPlanEnrollment {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @Column(name = "meal_plan_id", nullable = false)
    private UUID mealPlanId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentSource source;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    public static MealPlanEnrollment of(UUID mealPlanId, UUID clientId, EnrollmentSource source, Instant now) {
        MealPlanEnrollment enrollment = new MealPlanEnrollment();
        enrollment.mealPlanId = mealPlanId;
        enrollment.clientId = clientId;
        enrollment.source = source;
        enrollment.enrolledAt = now;
        return enrollment;
    }

    /** Una recompensa convierte una inscripción propia en desbloqueada. */
    public void unlockByReward() {
        this.source = EnrollmentSource.REWARD;
    }
}
