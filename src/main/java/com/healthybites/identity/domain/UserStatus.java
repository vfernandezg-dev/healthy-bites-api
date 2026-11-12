package com.healthybites.identity.domain;

/** Una cuenta desactivada no puede iniciar sesión y sus sesiones abiertas quedan revocadas. */
public enum UserStatus {
    ACTIVE,
    DEACTIVATED
}
