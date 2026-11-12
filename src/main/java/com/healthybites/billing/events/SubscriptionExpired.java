package com.healthybites.billing.events;

import com.healthybites.billing.Tier;
import java.util.UUID;

/** Terminó el período pagado de una suscripción: el usuario vuelve al nivel que le quede. */
public record SubscriptionExpired(UUID subscriptionId, UUID userId, Tier tier) {}
