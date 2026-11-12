/**
 * Progreso del cliente: metas de peso (una activa a la vez, con historial de estados), pesajes por día, registros
 * diarios de hábitos y una recomendación de peso saludable a partir del perfil.
 *
 * <p>Eventos en {@code events}: {@code HabitLogged} (lo escuchan las rachas de rewards) y {@code GoalCompleted}.
 */
@ApplicationModule(displayName = "Progress")
@NullMarked
package com.healthybites.progress;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
