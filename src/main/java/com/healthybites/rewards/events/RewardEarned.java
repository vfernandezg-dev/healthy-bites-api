package com.healthybites.rewards.events;

import java.util.UUID;

/** Un cliente completó la racha de una recompensa y su premio ya está desbloqueado. */
public record RewardEarned(UUID rewardId, UUID clientId) {}
