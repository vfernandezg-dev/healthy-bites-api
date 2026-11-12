package com.healthybites.billing.infra;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Credenciales de la API REST de PayPal ({@code PAYPAL_CLIENT_ID}, {@code PAYPAL_CLIENT_SECRET}). Son opcionales al
 * arrancar, para poder desarrollar sin cuenta de PayPal: el checkout responde 503 con un mensaje claro si faltan.
 */
@Validated
@ConfigurationProperties("healthybites.paypal")
public record PayPalProperties(
        @Nullable String clientId,
        @Nullable String clientSecret,
        @NotBlank @URL String apiBase) {

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }
}
