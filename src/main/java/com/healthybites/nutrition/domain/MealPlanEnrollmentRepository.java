package com.healthybites.nutrition.domain;

import com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealPlanEnrollmentRepository extends JpaRepository<MealPlanEnrollment, UUID> {

    Optional<MealPlanEnrollment> findByMealPlanIdAndClientId(UUID mealPlanId, UUID clientId);

    List<MealPlanEnrollment> findByClientIdOrderByEnrolledAtDesc(UUID clientId);

    boolean existsByMealPlanIdAndClientIdAndSource(UUID mealPlanId, UUID clientId, EnrollmentSource source);

    /** Planes que el cliente desbloqueó con recompensas (para marcar el catálogo de una vez). */
    @Query("""
            select e.mealPlanId from MealPlanEnrollment e
            where e.clientId = :clientId
              and e.source = com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource.REWARD""")
    Set<UUID> rewardUnlockedPlanIds(@Param("clientId") UUID clientId);

    /** Inscripciones en los planes de un nutricionista (sus clientes). */
    @Query("""
            select e from MealPlanEnrollment e, MealPlan p
            where p.id = e.mealPlanId and p.authorId = :authorId order by e.enrolledAt desc""")
    List<MealPlanEnrollment> findByAuthor(@Param("authorId") UUID authorId);
}
