package com.healthybites.shared.security;

/**
 * Rol de una cuenta. Cada cuenta tiene exactamente uno; el web-app los muestra en español. Vive en {@code shared}
 * porque la autorización (reglas por ruta, {@link CurrentUser}, {@link ClientOnly}) lo usa en todos los módulos.
 */
public enum Role {
    ADMIN,
    CLIENT,
    NUTRITIONIST
}
