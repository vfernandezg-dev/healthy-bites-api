package com.healthybites.rewards;

import java.util.Optional;
import java.util.UUID;

/** Datos de una recompensa para otros módulos (por ejemplo, el correo de enhorabuena de notifications). */
public interface RewardDirectory {

    Optional<RewardSummary> find(UUID rewardId);

    enum PrizeKind {
        CONTENT,
        MEAL_PLAN
    }

    /** La recompensa, los días seguidos que pide y el premio que desbloquea. */
    record RewardSummary(String title, int streakDays, PrizeKind prizeKind, String prizeTitle) {}
}
