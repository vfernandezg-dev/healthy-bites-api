package com.healthybites.nutrition.web;

import com.healthybites.nutrition.app.MealPlanService.PlanCard;
import com.healthybites.nutrition.app.NutritionistPlanService;
import com.healthybites.nutrition.domain.Meal;
import com.healthybites.nutrition.domain.NutritionEnums.MealPlanStatus;
import com.healthybites.nutrition.web.NutritionDtos.MealPlanDetailResponse;
import com.healthybites.nutrition.web.NutritionDtos.MealPlanRequest;
import com.healthybites.nutrition.web.NutritionDtos.MealPlanSummaryResponse;
import com.healthybites.nutrition.web.NutritionDtos.MealRequest;
import com.healthybites.nutrition.web.NutritionDtos.MealResponse;
import com.healthybites.nutrition.web.NutritionDtos.NutritionistClientResponse;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Locations;
import com.healthybites.shared.web.PublicationSummaryResponse;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nutritionist")
@Tag(name = "Nutricionista · Planes", description = "Planes de alimentación propios, sus comidas y sus clientes")
class NutritionistMealPlanController {

    private static final Set<String> SORTABLE = Set.of("createdAt", "updatedAt", "title", "status");

    private final NutritionistPlanService plans;

    NutritionistMealPlanController(NutritionistPlanService plans) {
        this.plans = plans;
    }

    @Operation(summary = "Mis planes (todos los estados, o solo uno con ?status=)")
    @GetMapping("/meal-plans")
    PagedModel<MealPlanSummaryResponse> mine(
            @RequestParam(required = false) @Nullable MealPlanStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return new PagedModel<>(plans.mine(CurrentUser.id(), status, Sorting.restrict(pageable, SORTABLE))
                .map(MealPlanSummaryResponse::of));
    }

    @Operation(summary = "Cuántos planes tengo en cada estado")
    @GetMapping("/meal-plans/summary")
    PublicationSummaryResponse summary() {
        Map<MealPlanStatus, Long> counts = plans.countByStatus(CurrentUser.id());
        return new PublicationSummaryResponse(
                counts.get(MealPlanStatus.DRAFT),
                counts.get(MealPlanStatus.PUBLISHED),
                counts.get(MealPlanStatus.ARCHIVED));
    }

    @Operation(summary = "Crea un plan en borrador")
    @PostMapping("/meal-plans")
    ResponseEntity<MealPlanDetailResponse> create(@Valid @RequestBody MealPlanRequest body) {
        PlanCard plan = plans.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(Locations.of(
                        "/nutritionist/meal-plans/{id}", plan.plan().requireId()))
                .body(MealPlanDetailResponse.of(plan));
    }

    @GetMapping("/meal-plans/{id}")
    MealPlanDetailResponse get(@PathVariable UUID id) {
        return MealPlanDetailResponse.of(plans.get(id, CurrentUser.id()));
    }

    @PutMapping("/meal-plans/{id}")
    MealPlanDetailResponse update(@PathVariable UUID id, @Valid @RequestBody MealPlanRequest body) {
        return MealPlanDetailResponse.of(plans.update(id, CurrentUser.id(), body.toData()));
    }

    @Operation(
            summary = "Borra un borrador",
            description = "Un plan publicado no se borra (409 plan-not-draft): se retira con POST /{id}/archive.")
    @DeleteMapping("/meal-plans/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        plans.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Publica el plan (necesita al menos una comida)")
    @PostMapping("/meal-plans/{id}/publish")
    MealPlanDetailResponse publish(@PathVariable UUID id) {
        return MealPlanDetailResponse.of(plans.publish(id, CurrentUser.id()));
    }

    @Operation(summary = "Retira el plan del catálogo (los clientes inscritos lo conservan)")
    @PostMapping("/meal-plans/{id}/archive")
    MealPlanDetailResponse archive(@PathVariable UUID id) {
        return MealPlanDetailResponse.of(plans.archive(id, CurrentUser.id()));
    }

    @PostMapping("/meal-plans/{id}/meals")
    ResponseEntity<MealResponse> addMeal(@PathVariable UUID id, @Valid @RequestBody MealRequest body) {
        Meal meal = plans.addMeal(id, CurrentUser.id(), body.toData());
        return ResponseEntity.created(
                        Locations.of("/nutritionist/meal-plans/{id}/meals/{mealId}", id, meal.requireId()))
                .body(MealResponse.of(meal));
    }

    @PutMapping("/meal-plans/{id}/meals/{mealId}")
    MealResponse updateMeal(@PathVariable UUID id, @PathVariable UUID mealId, @Valid @RequestBody MealRequest body) {
        return MealResponse.of(plans.updateMeal(id, mealId, CurrentUser.id(), body.toData()));
    }

    @DeleteMapping("/meal-plans/{id}/meals/{mealId}")
    ResponseEntity<Void> removeMeal(@PathVariable UUID id, @PathVariable UUID mealId) {
        plans.removeMeal(id, mealId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Clientes que siguen alguno de mis planes, los más recientes primero",
            description = "q busca por nombre, apellidos o correo sin distinguir mayúsculas ni tildes.")
    @GetMapping("/clients")
    PagedModel<NutritionistClientResponse> clients(
            @RequestParam(required = false) @Nullable @Size(max = 100, message = "Máximo 100 caracteres") String q,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(plans.clients(CurrentUser.id(), q, pageable).map(NutritionistClientResponse::of));
    }
}
