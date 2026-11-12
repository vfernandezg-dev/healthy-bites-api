package com.healthybites.nutrition.web;

import com.healthybites.nutrition.app.MealPlanService;
import com.healthybites.nutrition.domain.MealPlanEnrollment;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanGoal;
import com.healthybites.nutrition.web.NutritionDtos.EnrollRequest;
import com.healthybites.nutrition.web.NutritionDtos.EnrollmentResponse;
import com.healthybites.nutrition.web.NutritionDtos.MealPlanDetailResponse;
import com.healthybites.nutrition.web.NutritionDtos.MealPlanSummaryResponse;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Locations;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Planes de alimentación", description = "Catálogo, detalle e inscripciones de los clientes")
class MealPlanController {

    private static final Set<String> SORTABLE = Set.of("publishedAt", "title", "durationDays");

    private final MealPlanService mealPlans;

    MealPlanController(MealPlanService mealPlans) {
        this.mealPlans = mealPlans;
    }

    @Operation(summary = "Planes publicados; locked indica si el usuario no puede abrirlos")
    @GetMapping("/meal-plans")
    PagedModel<MealPlanSummaryResponse> catalog(
            @RequestParam(required = false) @Nullable MealPlanGoal goal,
            @ParameterObject @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return new PagedModel<>(mealPlans
                .catalog(CurrentUser.id(), isStaff(), goal, Sorting.restrict(pageable, SORTABLE))
                .map(MealPlanSummaryResponse::of));
    }

    @Operation(summary = "Detalle con comidas (403 subscription-required, con requiredTier, si está bloqueado)")
    @GetMapping("/meal-plans/{id}")
    MealPlanDetailResponse detail(@PathVariable UUID id) {
        return MealPlanDetailResponse.of(mealPlans.open(id, CurrentUser.id(), isStaff()));
    }

    @Operation(summary = "Planes que sigo")
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me/meal-plan-enrollments")
    List<EnrollmentResponse> enrollments() {
        return mealPlans.enrollments(CurrentUser.id()).stream()
                .map(e -> EnrollmentResponse.of(e.enrollment(), e.plan()))
                .toList();
    }

    @Operation(summary = "Empiezo a seguir un plan (hay que poder abrirlo)")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/me/meal-plan-enrollments")
    ResponseEntity<Void> enroll(@Valid @RequestBody EnrollRequest body) {
        MealPlanEnrollment enrollment = mealPlans.enroll(body.mealPlanId(), CurrentUser.id());
        return ResponseEntity.created(
                        Locations.of("/me/meal-plan-enrollments/{mealPlanId}", enrollment.getMealPlanId()))
                .build();
    }

    @Operation(summary = "Dejo de seguir un plan (los desbloqueados con una recompensa se conservan)")
    @PreAuthorize("hasRole('CLIENT')")
    @DeleteMapping("/me/meal-plan-enrollments/{mealPlanId}")
    ResponseEntity<Void> unenroll(@PathVariable UUID mealPlanId) {
        mealPlans.unenroll(mealPlanId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    private static boolean isStaff() {
        return CurrentUser.hasRole("NUTRITIONIST") || CurrentUser.hasRole("ADMIN");
    }
}
