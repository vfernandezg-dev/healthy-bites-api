package com.healthybites.rewards.app;

import com.healthybites.content.ContentAccess;
import com.healthybites.nutrition.MealPlanAccess;
import com.healthybites.rewards.RewardDirectory;
import com.healthybites.rewards.domain.Reward;
import com.healthybites.rewards.domain.RewardRepository;
import com.healthybites.rewards.domain.RewardStreak;
import com.healthybites.rewards.domain.RewardStreakRepository;
import com.healthybites.shared.web.ApiException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recompensas: el catálogo con el progreso del cliente, sus rachas y la gestión del nutricionista. */
@Service
public class RewardService implements RewardDirectory {

    private final RewardRepository rewards;
    private final RewardStreakRepository streaks;
    private final ContentAccess contents;
    private final MealPlanAccess mealPlans;

    public RewardService(
            RewardRepository rewards,
            RewardStreakRepository streaks,
            ContentAccess contents,
            MealPlanAccess mealPlans) {
        this.rewards = rewards;
        this.streaks = streaks;
        this.contents = contents;
        this.mealPlans = mealPlans;
    }

    public enum TargetType {
        CONTENT,
        MEAL_PLAN
    }

    /** A qué da acceso la recompensa. */
    public record Target(TargetType type, UUID id, String title) {}

    public record RewardView(
            Reward reward, Target target, @Nullable RewardStreak streak) {}

    @Transactional(readOnly = true)
    public List<RewardView> catalog(UUID clientId) {
        Map<UUID, RewardStreak> mine = streaks.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .collect(Collectors.toMap(RewardStreak::getRewardId, Function.identity()));
        return rewards.findByActiveTrueOrderByRequiredStreakDaysAsc().stream()
                .map(reward -> new RewardView(reward, target(reward), mine.get(reward.requireId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RewardView> streaks(UUID clientId) {
        List<RewardStreak> mine = streaks.findByClientIdOrderByCreatedAtDesc(clientId);
        Map<UUID, Reward> byId = rewards
                .findAllById(mine.stream().map(RewardStreak::getRewardId).toList())
                .stream()
                .collect(Collectors.toMap(Reward::requireId, Function.identity()));
        return mine.stream()
                .filter(streak -> byId.containsKey(streak.getRewardId()))
                .map(streak -> {
                    Reward reward = byId.get(streak.getRewardId());
                    return new RewardView(reward, target(reward), streak);
                })
                .toList();
    }

    /** Empieza una racha hacia una recompensa activa. Solo una por recompensa. */
    @Transactional
    public RewardView start(UUID rewardId, UUID clientId) {
        Reward reward = rewards.findById(rewardId)
                .filter(Reward::isActive)
                .orElseThrow(() -> ApiException.notFound("reward-not-found", "La recompensa no existe"));
        if (streaks.findByRewardIdAndClientId(rewardId, clientId).isPresent()) {
            throw ApiException.conflict("streak-exists", "Ya llevas una racha hacia esta recompensa");
        }
        RewardStreak streak = streaks.save(RewardStreak.start(rewardId, clientId));
        return new RewardView(reward, target(reward), streak);
    }

    @Transactional
    public void abandon(UUID streakId, UUID clientId) {
        streaks.findByIdAndClientId(streakId, clientId).ifPresent(streak -> {
            if (streak.isCompleted()) {
                throw ApiException.conflict("streak-completed", "Esta racha ya está completada");
            }
            streaks.delete(streak);
        });
    }

    /** Para otros módulos: la recompensa con su premio, esté activa o no (las ya ganadas conservan su premio). */
    @Override
    @Transactional(readOnly = true)
    public Optional<RewardSummary> find(UUID rewardId) {
        return rewards.findById(rewardId).map(reward -> {
            Target target = target(reward);
            PrizeKind kind = target.type() == TargetType.CONTENT ? PrizeKind.CONTENT : PrizeKind.MEAL_PLAN;
            return new RewardSummary(reward.getTitle(), reward.getRequiredStreakDays(), kind, target.title());
        });
    }

    // ---------------------------------------------------------------- nutricionista

    @Transactional(readOnly = true)
    public List<RewardView> mine(UUID authorId) {
        return rewards.findByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(reward -> new RewardView(reward, target(reward), null))
                .toList();
    }

    @Transactional
    public RewardView create(UUID authorId, Reward.Data data) {
        checkTarget(authorId, data);
        Reward reward = rewards.save(Reward.create(authorId, data));
        return new RewardView(reward, target(reward), null);
    }

    @Transactional
    public RewardView update(UUID rewardId, UUID authorId, Reward.Data data) {
        checkTarget(authorId, data);
        Reward reward = owned(rewardId, authorId);
        reward.update(data);
        return new RewardView(reward, target(reward), null);
    }

    /** Deja de ofrecerse (no se borra: las rachas completadas conservan su premio). */
    @Transactional
    public void deactivate(UUID rewardId, UUID authorId) {
        owned(rewardId, authorId).deactivate();
    }

    private void checkTarget(UUID authorId, Reward.Data data) {
        boolean content = data.contentItemId() != null;
        boolean plan = data.mealPlanId() != null;
        boolean valid = content != plan
                && (content
                        ? contents.isPublishedBy(data.contentItemId(), authorId)
                        : mealPlans.isPublishedBy(data.mealPlanId(), authorId));
        if (!valid) {
            throw ApiException.unprocessable(
                    "invalid-reward-target",
                    "El premio tiene que ser un contenido o un plan tuyo y publicado (solo uno de los dos)");
        }
    }

    private Target target(Reward reward) {
        if (reward.getContentItemId() != null) {
            UUID id = reward.getContentItemId();
            return new Target(TargetType.CONTENT, id, contents.titleOf(id).orElse(""));
        }
        UUID id = Objects.requireNonNull(reward.getMealPlanId());
        return new Target(TargetType.MEAL_PLAN, id, mealPlans.titleOf(id).orElse(""));
    }

    private Reward owned(UUID rewardId, UUID authorId) {
        return rewards.findByIdAndAuthorId(rewardId, authorId)
                .orElseThrow(() -> ApiException.notFound("reward-not-found", "La recompensa no existe"));
    }
}
