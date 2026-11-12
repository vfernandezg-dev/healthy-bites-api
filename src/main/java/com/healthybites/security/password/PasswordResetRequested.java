package com.healthybites.security.password;

import java.time.Duration;

/**
 * Se ha creado un enlace de restablecimiento. Solo vive en memoria: lleva el token en claro dentro del enlace y
 * nunca se persiste (ni en la base de datos ni en un registro de eventos).
 */
public record PasswordResetRequested(String email, String link, Duration validFor) {

    @Override
    public String toString() {
        // Que el enlace (y por tanto el token) no acabe en un log por accidente.
        return "PasswordResetRequested[email=" + email + ", validFor=" + validFor + "]";
    }
}
