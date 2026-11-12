package com.healthybites.nutrition.domain;

import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    Page<MealPlan> findByStatus(MealPlanStatus status, Pageable pageable);

    Page<MealPlan> findByStatusAndGoal(MealPlanStatus status, MealPlanGoal goal, Pageable pageable);

    Page<MealPlan> findByAuthorId(UUID authorId, Pageable pageable);

    Optional<MealPlan> findByIdAndAuthorId(UUID id, UUID authorId);

    @Query("select count(p) > 0 from MealPlan p where p.id = :id and p.authorId = :authorId"
            + " and p.status = com.healthybites.nutrition.domain.NutritionEnums.MealPlanStatus.PUBLISHED")
    boolean isPublishedBy(@Param("id") UUID id, @Param("authorId") UUID authorId);
}
