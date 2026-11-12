package com.healthybites.billing.events;

import com.healthybites.billing.Tier;
import java.util.UUID;

/** Una suscripción se ha pagado y está activa. */
public record SubscriptionActivated(UUID subscriptionId, UUID userId, Tier tier, UUID paymentId) {}
