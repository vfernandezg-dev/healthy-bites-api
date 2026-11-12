/**
 * Planes de alimentación: los crean y publican los nutricionistas, con sus comidas por día. Un cliente abre un plan
 * si es BASIC, si su suscripción incluye el nivel requerido o si lo desbloqueó con una recompensa.
 *
 * <p>API para otros módulos: {@link com.healthybites.nutrition.MealPlanAccess}.
 */
@ApplicationModule(displayName = "Nutrition")
@NullMarked
package com.healthybites.nutrition;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
