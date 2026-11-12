package com.healthybites.nutrition.domain;

import com.healthybites.nutrition.domain.NutritionEnums.EnrollmentSource;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    /** Clientes de los planes del autor con su primera inscripción, los más recientes primero. */
    @Query(value = """
                    select e.clientId as clientId, min(e.enrolledAt) as since
                    from MealPlanEnrollment e, MealPlan p
                    where p.id = e.mealPlanId and p.authorId = :authorId
                    group by e.clientId order by min(e.enrolledAt) desc""", countQuery = """
                    select count(distinct e.clientId) from MealPlanEnrollment e, MealPlan p
                    where p.id = e.mealPlanId and p.authorId = :authorId""")
    Page<ClientSince> findClientsOf(@Param("authorId") UUID authorId, Pageable pageable);

    /** Igual que {@link #findClientsOf}, solo entre esos clientes (el resultado de una búsqueda). */
    @Query(value = """
                    select e.clientId as clientId, min(e.enrolledAt) as since
                    from MealPlanEnrollment e, MealPlan p
                    where p.id = e.mealPlanId and p.authorId = :authorId and e.clientId in :clientIds
                    group by e.clientId order by min(e.enrolledAt) desc""", countQuery = """
                    select count(distinct e.clientId) from MealPlanEnrollment e, MealPlan p
                    where p.id = e.mealPlanId and p.authorId = :authorId and e.clientId in :clientIds""")
    Page<ClientSince> findClientsOfAmong(
            @Param("authorId") UUID authorId, @Param("clientIds") Collection<UUID> clientIds, Pageable pageable);

    @Query("""
            select distinct e.clientId from MealPlanEnrollment e, MealPlan p
            where p.id = e.mealPlanId and p.authorId = :authorId""")
    Set<UUID> findClientIdsOf(@Param("authorId") UUID authorId);

    /** Las inscripciones de esos clientes en los planes del autor (para listar sus planes). */
    @Query("""
            select e from MealPlanEnrollment e, MealPlan p
            where p.id = e.mealPlanId and p.authorId = :authorId and e.clientId in :clientIds
            order by e.enrolledAt desc""")
    List<MealPlanEnrollment> findByAuthorAndClientIdIn(
            @Param("authorId") UUID authorId, @Param("clientIds") Collection<UUID> clientIds);

    interface ClientSince {
        UUID getClientId();

        Instant getSince();
    }
}
