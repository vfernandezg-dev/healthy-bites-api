package com.healthybites.profiles.app;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Registros públicos permitidos desde una misma IP ({@code healthybites.security.rate-limit.registration*}). */
@ConfigurationProperties("healthybites.security.rate-limit")
@Validated
public record RegistrationProperties(
        @Positive int registrationsPerIp, @NotNull Duration registrationWindow) {}
