package com.healthybites.contact.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Formulario de contacto de la landing ({@code healthybites.contact.*}, valores en application.yml). */
@ConfigurationProperties("healthybites.contact")
@Validated
public record ContactProperties(
        @NotBlank String recipient, @Valid @NotNull RateLimit rateLimit) {

    /** Límites antiabuso: por cliente (IP) y global (acota el total aunque se falsifique la IP). */
    public record RateLimit(
            @Positive int perClient,
            @NotNull Duration perClientWindow,
            @Positive int global,
            @NotNull Duration globalWindow) {}
}
