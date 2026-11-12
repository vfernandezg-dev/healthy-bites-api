package com.healthybites.progress.web;

import com.healthybites.progress.app.GoalService;
import com.healthybites.progress.app.HabitLogService;
import com.healthybites.progress.domain.Goal;
import com.healthybites.progress.domain.HabitLog;
import com.healthybites.progress.domain.WeightEntry;
import com.healthybites.progress.web.ProgressDtos.GoalRequest;
import com.healthybites.progress.web.ProgressDtos.GoalResponse;
import com.healthybites.progress.web.ProgressDtos.HabitLogRequest;
import com.healthybites.progress.web.ProgressDtos.HabitLogResponse;
import com.healthybites.progress.web.ProgressDtos.RecommendationResponse;
import com.healthybites.progress.web.ProgressDtos.StatusChangeResponse;
import com.healthybites.progress.web.ProgressDtos.WeightEntryRequest;
import com.healthybites.progress.web.ProgressDtos.WeightEntryResponse;
import com.healthybites.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/me")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "Progreso", description = "Metas, pesajes, hábitos y recomendación (solo clientes)")
class ProgressController {

    private final GoalService goals;
    private final HabitLogService habits;

    ProgressController(GoalService goals, HabitLogService habits) {
        this.goals = goals;
        this.habits = habits;
    }

    // ---------------------------------------------------------------- metas

    @GetMapping("/goals")
    List<GoalResponse> goals(@RequestParam(required = false) Goal.@Nullable Status status) {
        return goals.list(CurrentUser.id(), status).stream()
                .map(GoalResponse::of)
                .toList();
    }

    @Operation(summary = "Crea una meta (solo puede haber una activa)")
    @PostMapping("/goals")
    ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest body) {
        Goal goal = goals.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(URI.create("/me/goals/" + goal.requireId()))
                .body(GoalResponse.of(goal));
    }

    @GetMapping("/goals/{id}")
    GoalResponse goal(@PathVariable UUID id) {
        return GoalResponse.of(goals.get(id, CurrentUser.id()));
    }

    @PutMapping("/goals/{id}")
    GoalResponse updateGoal(@PathVariable UUID id, @Valid @RequestBody GoalRequest body) {
        return GoalResponse.of(goals.update(id, CurrentUser.id(), body.toData()));
    }

    @PostMapping("/goals/{id}/complete")
    GoalResponse complete(@PathVariable UUID id) {
        return GoalResponse.of(goals.complete(id, CurrentUser.id()));
    }

    @PostMapping("/goals/{id}/abandon")
    GoalResponse abandon(@PathVariable UUID id) {
        return GoalResponse.of(goals.abandon(id, CurrentUser.id()));
    }

    @GetMapping("/goals/{id}/status-history")
    List<StatusChangeResponse> history(@PathVariable UUID id) {
        return goals.history(id, CurrentUser.id()).stream()
                .map(StatusChangeResponse::of)
                .toList();
    }

    @Operation(summary = "Rango de peso saludable para tu altura y estimación del tiempo hasta el objetivo")
    @GetMapping("/goals/{id}/recommendation")
    RecommendationResponse recommendation(@PathVariable UUID id) {
        return RecommendationResponse.of(goals.recommendation(id, CurrentUser.id()));
    }

    // ---------------------------------------------------------------- pesajes

    @Operation(summary = "Pesajes de la meta en un rango de fechas (todos si no hay rango), del más antiguo al último")
    @GetMapping("/goals/{id}/weight-entries")
    List<WeightEntryResponse> weights(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate to) {
        return goals.weights(id, CurrentUser.id(), from, to).stream()
                .map(WeightEntryResponse::of)
                .toList();
    }

    @PostMapping("/goals/{id}/weight-entries")
    ResponseEntity<WeightEntryResponse> addWeight(@PathVariable UUID id, @Valid @RequestBody WeightEntryRequest body) {
        WeightEntry entry = goals.addWeight(id, CurrentUser.id(), body.toData());
        return ResponseEntity.created(URI.create("/me/goals/" + id + "/weight-entries/" + entry.requireId()))
                .body(WeightEntryResponse.of(entry));
    }

    @PutMapping("/goals/{id}/weight-entries/{entryId}")
    WeightEntryResponse updateWeight(
            @PathVariable UUID id, @PathVariable UUID entryId, @Valid @RequestBody WeightEntryRequest body) {
        return WeightEntryResponse.of(goals.updateWeight(id, entryId, CurrentUser.id(), body.toData()));
    }

    @DeleteMapping("/goals/{id}/weight-entries/{entryId}")
    ResponseEntity<Void> deleteWeight(@PathVariable UUID id, @PathVariable UUID entryId) {
        goals.deleteWeight(id, entryId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- hábitos

    @Operation(summary = "Registros de hábitos en un rango (por defecto, los últimos 30 días)")
    @GetMapping("/habit-logs")
    List<HabitLogResponse> habitLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate to) {
        return habits.list(CurrentUser.id(), from, to).stream()
                .map(HabitLogResponse::of)
                .toList();
    }

    @Operation(summary = "Registra los hábitos de hoy (o de ayer); cuenta para las rachas")
    @PostMapping("/habit-logs")
    ResponseEntity<HabitLogResponse> logHabits(@Valid @RequestBody HabitLogRequest body) {
        HabitLog log = habits.log(CurrentUser.id(), body.loggedOn(), body.toScores());
        return ResponseEntity.created(URI.create("/me/habit-logs/" + log.requireId()))
                .body(HabitLogResponse.of(log));
    }

    @PutMapping("/habit-logs/{id}")
    HabitLogResponse updateHabits(@PathVariable UUID id, @Valid @RequestBody HabitLogRequest body) {
        return HabitLogResponse.of(habits.update(id, CurrentUser.id(), body.toScores()));
    }

    @DeleteMapping("/habit-logs/{id}")
    ResponseEntity<Void> deleteHabits(@PathVariable UUID id) {
        habits.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}
