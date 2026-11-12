package com.healthybites.nutrition;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Para rewards: comprobar planes de un nutricionista y desbloquearlos como premio. */
public interface MealPlanAccess {

    /** ¿El plan existe, está publicado y es de ese nutricionista? */
    boolean isPublishedBy(UUID mealPlanId, UUID authorId);

    /** Desbloquea el plan para el cliente (inscripción de origen REWARD). No falla si ya lo tenía. */
    void grant(UUID mealPlanId, UUID clientId);

    /** Títulos de los planes que existen, por id, en una sola consulta (para listados). */
    Map<UUID, String> titlesOf(Collection<UUID> mealPlanIds);
}
