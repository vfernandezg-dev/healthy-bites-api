package com.healthybites.rewards.app;

import com.healthybites.content.ContentAccess;
import com.healthybites.nutrition.MealPlanAccess;
import com.healthybites.progress.events.HabitLogged;
import com.healthybites.rewards.domain.Reward;
import com.healthybites.rewards.domain.RewardRepository;
import com.healthybites.rewards.domain.RewardStreak;
import com.healthybites.rewards.domain.RewardStreakRepository;
import com.healthybites.rewards.events.RewardEarned;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Avanza las rachas abiertas del cliente cada vez que registra sus hábitos. Escucha un evento persistido de
 * progress: si algo falla, Spring Modulith lo reintenta al arrancar, así que ninguna racha se pierde.
 */
@Slf4j
@Component
class StreakListener {

    private final RewardStreakRepository streaks;
    private final RewardRepository rewards;
    private final ContentAccess contents;
    private final MealPlanAccess mealPlans;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    StreakListener(
            RewardStreakRepository streaks,
            RewardRepository rewards,
            ContentAccess contents,
            MealPlanAccess mealPlans,
            ApplicationEventPublisher events,
            Clock clock) {
        this.streaks = streaks;
        this.rewards = rewards;
        this.contents = contents;
        this.mealPlans = mealPlans;
        this.events = events;
        this.clock = clock;
    }

    @ApplicationModuleListener
    void on(HabitLogged event) {
        List<RewardStreak> open = streaks.findByClientIdAndCompletedAtIsNull(event.clientId());
        Map<UUID, Reward> byId = rewards
                .findAllById(open.stream().map(RewardStreak::getRewardId).toList())
                .stream()
                .collect(Collectors.toMap(Reward::requireId, Function.identity()));
        for (RewardStreak streak : open) {
            Reward reward = byId.get(streak.getRewardId());
            if (reward == null) {
                continue;
            }
            if (streak.count(event.loggedOn(), reward.getRequiredStreakDays(), clock.instant())
                    == RewardStreak.Outcome.COMPLETED) {
                grant(reward, event);
                events.publishEvent(new RewardEarned(reward.requireId(), event.clientId()));
                log.info("Recompensa {} conseguida por el cliente {}", reward.getId(), event.clientId());
            }
        }
    }

    private void grant(Reward reward, HabitLogged event) {
        if (reward.getContentItemId() != null) {
            contents.grant(reward.getContentItemId(), event.clientId());
        } else if (reward.getMealPlanId() != null) {
            mealPlans.grant(reward.getMealPlanId(), event.clientId());
        }
    }
}
