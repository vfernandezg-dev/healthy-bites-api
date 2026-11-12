/**
 * Contenido: artículos, vídeos y guías de los nutricionistas, con portada pública y fichero protegido. Un usuario
 * abre un contenido si es BASIC, si su suscripción incluye el nivel requerido o si lo desbloqueó (recompensa).
 *
 * <p>API para otros módulos: {@link com.healthybites.content.ContentAccess}.
 */
@ApplicationModule(displayName = "Content")
@NullMarked
package com.healthybites.content;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
