package com.healthybites.rewards.web;

import com.healthybites.rewards.app.RewardService;
import com.healthybites.rewards.app.RewardService.RewardView;
import com.healthybites.rewards.web.RewardDtos.RewardRequest;
import com.healthybites.rewards.web.RewardDtos.RewardResponse;
import com.healthybites.rewards.web.RewardDtos.StartStreakRequest;
import com.healthybites.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Recompensas", description = "Rachas de los clientes y recompensas de los nutricionistas")
class RewardController {

    private final RewardService rewards;

    RewardController(RewardService rewards) {
        this.rewards = rewards;
    }

    @Operation(summary = "Recompensas activas con mi racha hacia cada una")
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/rewards")
    List<RewardResponse> catalog() {
        return rewards.catalog(CurrentUser.id()).stream()
                .map(RewardResponse::of)
                .toList();
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me/streaks")
    List<RewardResponse> streaks() {
        return rewards.streaks(CurrentUser.id()).stream()
                .map(RewardResponse::of)
                .toList();
    }

    @Operation(summary = "Empieza una racha: cuenta cada día que registres tus hábitos")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/me/streaks")
    ResponseEntity<RewardResponse> start(@Valid @RequestBody StartStreakRequest body) {
        RewardView view = rewards.start(body.rewardId(), CurrentUser.id());
        return ResponseEntity.created(URI.create("/me/streaks/" + view.streak().requireId()))
                .body(RewardResponse.of(view));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @DeleteMapping("/me/streaks/{id}")
    ResponseEntity<Void> abandon(@PathVariable UUID id) {
        rewards.abandon(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- nutricionista

    @GetMapping("/nutritionist/rewards")
    List<RewardResponse> mine() {
        return rewards.mine(CurrentUser.id()).stream().map(RewardResponse::of).toList();
    }

    @Operation(summary = "Crea una recompensa que desbloquea un contenido o un plan propio y publicado")
    @PostMapping("/nutritionist/rewards")
    ResponseEntity<RewardResponse> create(@Valid @RequestBody RewardRequest body) {
        RewardView view = rewards.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(
                        URI.create("/nutritionist/rewards/" + view.reward().requireId()))
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
