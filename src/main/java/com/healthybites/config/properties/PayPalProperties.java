package com.healthybites.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Credenciales de la API REST de PayPal ({@code PAYPAL_CLIENT_ID}, {@code PAYPAL_CLIENT_SECRET}). Son opcionales
 * al arrancar, para poder desarrollar sin cuenta de PayPal: el checkout falla con un error claro si faltan.
 */
@Validated
@ConfigurationProperties("healthybites.paypal")
public record PayPalProperties(String clientId, String clientSecret, @NotBlank @URL String apiBase) {

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }
}
