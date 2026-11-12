package com.healthybites.identity.app;

import java.time.Duration;

/**
 * Se ha creado un enlace de restablecimiento. Evento interno de identity que solo vive en memoria: lleva el token en
 * claro dentro del enlace, así que nunca se persiste (ni en la base de datos ni en el registro de eventos).
 */
public record PasswordResetRequested(String email, String firstName, String link, Duration validFor) {

    @Override
    public String toString() {
        // Que el enlace (y por tanto el token) no acabe en un log por accidente.
        return "PasswordResetRequested[email=" + email + ", validFor=" + validFor + "]";
    }
}
