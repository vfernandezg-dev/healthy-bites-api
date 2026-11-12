/**
 * Recompensas: un nutricionista premia N días seguidos registrando hábitos con un contenido o un plan suyo. Las
 * rachas avanzan con el evento {@code HabitLogged} de progress: como mucho un día por fecha y un hueco las reinicia.
 * Al completarse, se desbloquea el premio y se publica {@code RewardEarned}.
 */
@ApplicationModule(displayName = "Rewards")
@NullMarked
package com.healthybites.rewards;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
