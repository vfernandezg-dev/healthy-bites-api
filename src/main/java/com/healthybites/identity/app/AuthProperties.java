package com.healthybites.identity.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Límites de la sesión y validez de los enlaces ({@code healthybites.security.*}, valores en application.yml). Si
 * falta alguno o no es válido, la aplicación no arranca.
 */
@ConfigurationProperties("healthybites.security")
@Validated
public record AuthProperties(
        @Valid @NotNull RateLimit rateLimit,
        @Valid @NotNull PasswordReset passwordReset,
        @Valid @NotNull Invitations invitations) {

    public record RateLimit(
            @Positive int loginPerAccount,
            @Positive int loginPerIp,
            @NotNull Duration loginWindow,
            @Positive int refreshPerIp,
            @NotNull Duration refreshWindow,
            @Positive int passwordResetPerAccount,
            @Positive int passwordResetPerIp,
            @NotNull Duration passwordResetWindow) {}

    public record PasswordReset(@NotNull Duration tokenTtl) {}

    public record Invitations(@NotNull Duration validity) {}
}
