package com.healthybites.shared.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Datos públicos de la aplicación.
 *
 * @param frontendUrl  origen del web-app ({@code APP_FRONTEND_URL}): enlaces de los correos y vuelta de PayPal
 * @param apiPublicUrl URL pública de esta API ({@code API_PUBLIC_URL}), con el context path; la usa OpenAPI
 * @param timeZone     zona horaria de las fechas que leen las personas, por ejemplo en los correos
 *                     ({@code APP_TIME_ZONE}). La API siempre guarda y devuelve instantes en UTC.
 */
@Validated
@ConfigurationProperties("healthybites.app")
public record AppProperties(
        @NotBlank(message = "Falta APP_FRONTEND_URL (ver .env.example)") @URL
        String frontendUrl,

        @NotBlank(message = "Falta API_PUBLIC_URL (ver .env.example)") @URL
        String apiPublicUrl,

        @NotNull @DefaultValue("America/Lima") ZoneId timeZone) {

    /** URL absoluta de una ruta del web-app, sin barras duplicadas. */
    public String frontend(String path) {
        return frontendUrl.replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
    }
}
