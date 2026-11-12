package com.healthybites.config.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Base64;

/**
 * Access tokens JWT (HS512). El secreto llega siempre del entorno ({@code JWT_SECRET}); la aplicación no arranca
 * si falta o si es demasiado corto.
 *
 * @param secret         clave en Base64 y en una sola línea; HS512 necesita al menos 64 bytes. Generar con
 *                       {@code openssl rand -base64 64 | tr -d '\r\n'}
 * @param accessTokenTtl validez del access token; corta, porque la sesión se renueva con el refresh token
 * @param issuer         emisor ({@code iss}) que se firma y se exige al validar
 * @param audience       audiencia ({@code aud}) que se firma y se exige al validar
 */
@Validated
@ConfigurationProperties("healthybites.security.jwt")
public record JwtProperties(
        @NotBlank(message = "Falta JWT_SECRET: genéralo con `openssl rand -base64 64 | tr -d '\\r\\n'` (ver .env.example)")
        String secret,
        @NotNull Duration accessTokenTtl,
        @NotBlank String issuer,
        @NotBlank String audience) {

    static final int MIN_KEY_BYTES = 64;

    @AssertTrue(message = "JWT_SECRET debe ser Base64 de al menos 64 bytes (HS512) y en una sola línea: "
            + "`openssl rand -base64 64 | tr -d '\\r\\n'`")
    public boolean isSecretStrongEnough() {
        if (secret == null || secret.isBlank()) {
            return true; // de esto ya informa @NotBlank
        }
        return decodedSecret() != null && decodedSecret().length >= MIN_KEY_BYTES;
    }

    /** Bytes de la clave, o {@code null} si el secreto no es Base64 válido. */
    public byte[] decodedSecret() {
        try {
            return Base64.getDecoder().decode(secret.trim());
        } catch (IllegalArgumentException notBase64) {
            return null;
        }
    }
}
