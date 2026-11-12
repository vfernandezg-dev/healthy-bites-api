package com.healthybites.config.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Base64;

/**
 * Firma de los tokens JWT (HS512). El secreto llega siempre del entorno ({@code JWT_SECRET}); la aplicación
 * no arranca si falta o si es demasiado corto.
 *
 * @param secret   clave en Base64 y en una sola línea; HS512 necesita al menos 64 bytes. Generar con
 *                 {@code openssl rand -base64 64 | tr -d '\r\n'}
 * @param validity tiempo de validez de cada token
 */
@Validated
@ConfigurationProperties("healthybites.security.jwt")
public record JwtProperties(
        @NotBlank(message = "Falta JWT_SECRET: genéralo con `openssl rand -base64 64 | tr -d '\\r\\n'` (ver .env.example)")
        String secret,
        @NotNull Duration validity) {

    static final int MIN_KEY_BYTES = 64;

    @AssertTrue(message = "JWT_SECRET debe ser Base64 de al menos 64 bytes (HS512) y en una sola línea: "
            + "`openssl rand -base64 64 | tr -d '\\r\\n'`")
    public boolean isSecretStrongEnough() {
        if (secret == null || secret.isBlank()) {
            return true; // de esto ya informa @NotBlank
        }
        try {
            return Base64.getDecoder().decode(secret.trim()).length >= MIN_KEY_BYTES;
        } catch (IllegalArgumentException notBase64) {
            return false;
        }
    }
}
