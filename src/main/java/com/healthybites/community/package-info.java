/**
 * Comunidad: grupos públicos (cualquiera entra y lee) y privados (se pide entrar y el creador aprueba; solo los
 * miembros leen), publicaciones, comentarios y denuncias que modera un ADMIN.
 *
 * <p>API para otros módulos: {@link com.healthybites.community.GroupDirectory}. Eventos en {@code events}:
 * {@code MembershipRequested} (lo escucha notifications para avisar al creador).
 */
@ApplicationModule(displayName = "Community")
@NullMarked
package com.healthybites.community;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
