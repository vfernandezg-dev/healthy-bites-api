package com.healthybites.rewards.web;

import com.healthybites.rewards.app.RewardService;
import com.healthybites.rewards.app.RewardService.RewardView;
import com.healthybites.rewards.web.RewardDtos.RewardRequest;
import com.healthybites.rewards.web.RewardDtos.RewardResponse;
import com.healthybites.rewards.web.RewardDtos.StartStreakRequest;
import com.healthybites.shared.security.ClientOnly;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Locations;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Recompensas", description = "Rachas de los clientes y recompensas de los nutricionistas")
class RewardController {

    private static final Set<String> SORTABLE = Set.of("requiredStreakDays", "createdAt", "title");

    private final RewardService rewards;

    RewardController(RewardService rewards) {
        this.rewards = rewards;
    }

    @Operation(summary = "Recompensas activas con mi racha hacia cada una")
    @ClientOnly
    @GetMapping("/rewards")
    PagedModel<RewardResponse> catalog(
            @ParameterObject @PageableDefault(size = 20, sort = "requiredStreakDays") Pageable pageable) {
        return new PagedModel<>(rewards.catalog(CurrentUser.id(), Sorting.restrict(pageable, SORTABLE))
                .map(RewardResponse::of));
    }

    @ClientOnly
    @GetMapping("/me/streaks")
    List<RewardResponse> streaks() {
        return rewards.streaks(CurrentUser.id()).stream()
                .map(RewardResponse::of)
                .toList();
    }

    @Operation(summary = "Empieza una racha: cuenta cada día que registres tus hábitos")
    @ClientOnly
    @PostMapping("/me/streaks")
    ResponseEntity<RewardResponse> start(@Valid @RequestBody StartStreakRequest body) {
        RewardView view = rewards.start(body.rewardId(), CurrentUser.id());
        return ResponseEntity.created(Locations.of(
                        "/me/streaks/{id}",
                        Objects.requireNonNull(view.streak()).requireId()))
                .body(RewardResponse.of(view));
    }

    @ClientOnly
    @DeleteMapping("/me/streaks/{id}")
    ResponseEntity<Void> abandon(@PathVariable UUID id) {
        rewards.abandon(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- nutricionista

    @Operation(summary = "Mis recompensas (todas, o solo activas o retiradas con ?active=)")
    @GetMapping("/nutritionist/rewards")
    PagedModel<RewardResponse> mine(
            @RequestParam(required = false) @Nullable Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return new PagedModel<>(rewards.mine(CurrentUser.id(), active, Sorting.restrict(pageable, SORTABLE))
                .map(RewardResponse::of));
    }

    @Operation(summary = "Crea una recompensa que desbloquea un contenido o un plan propio y publicado")
    @PostMapping("/nutritionist/rewards")
    ResponseEntity<RewardResponse> create(@Valid @RequestBody RewardRequest body) {
        RewardView view = rewards.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(
                        Locations.of("/nutritionist/rewards/{id}", view.reward().requireId()))
                .body(RewardResponse.of(view));
    }

    @PutMapping("/nutritionist/rewards/{id}")
    RewardResponse update(@PathVariable UUID id, @Valid @RequestBody RewardRequest body) {
        return RewardResponse.of(rewards.update(id, CurrentUser.id(), body.toData()));
    }

    @Operation(summary = "Deja de ofrecer la recompensa (las rachas completadas conservan su premio)")
    @DeleteMapping("/nutritionist/rewards/{id}")
    ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        rewards.deactivate(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}
