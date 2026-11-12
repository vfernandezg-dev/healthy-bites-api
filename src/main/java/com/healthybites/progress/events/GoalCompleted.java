package com.healthybites.progress.events;

import java.util.UUID;

/** El cliente dio por cumplida una meta. */
public record GoalCompleted(UUID goalId, UUID clientId) {}
