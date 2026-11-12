package com.healthybites.nutrition;

import java.util.Optional;
import java.util.UUID;

/** Para rewards: comprobar planes de un nutricionista y desbloquearlos como premio. */
public interface MealPlanAccess {

    /** ¿El plan existe, está publicado y es de ese nutricionista? */
    boolean isPublishedBy(UUID mealPlanId, UUID authorId);

    /** Desbloquea el plan para el cliente (inscripción de origen REWARD). No falla si ya lo tenía. */
    void grant(UUID mealPlanId, UUID clientId);

    /** Título del plan (para avisos y listados), o vacío si no existe. */
    Optional<String> titleOf(UUID mealPlanId);
}
