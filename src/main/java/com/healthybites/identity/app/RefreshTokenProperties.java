package com.healthybites.identity.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Refresh tokens: opacos, rotatorios y en una cookie {@code httpOnly; SameSite=Strict}.
 *
 * @param idleTtl      caducidad si no se usa (cada rotación la renueva)
 * @param absoluteTtl  vida máxima de una sesión (familia de tokens), aunque se use a diario
 * @param reuseGrace   ventana en la que reutilizar un token recién rotado solo devuelve 401 (dos pestañas
 *                     refrescando a la vez); pasada la ventana, la reutilización revoca toda la familia
 * @param cookieName   nombre de la cookie; en producción con el prefijo {@code __Secure-}
 * @param cookieSecure atributo {@code Secure}; solo se desactiva en desarrollo sobre http
 * @param cookiePath   la cookie solo viaja a los endpoints de sesión
 */
@Validated
@ConfigurationProperties("healthybites.security.refresh")
public record RefreshTokenProperties(
        @NotNull Duration idleTtl,
        @NotNull Duration absoluteTtl,
        @NotNull Duration reuseGrace,
        @NotBlank String cookieName,
        boolean cookieSecure,
        @NotBlank String cookiePath) {}
