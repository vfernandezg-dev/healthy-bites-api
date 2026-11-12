package com.healthybites.shared.config;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * URLs públicas de la aplicación.
 *
 * @param frontendUrl  origen del web-app ({@code APP_FRONTEND_URL}): enlaces de los correos y vuelta de PayPal
 * @param apiPublicUrl URL pública de esta API ({@code API_PUBLIC_URL}), con el context path; la usa OpenAPI
 */
@Validated
@ConfigurationProperties("healthybites.app")
public record AppProperties(
        @NotBlank(message = "Falta APP_FRONTEND_URL (ver .env.example)") @URL
        String frontendUrl,

        @NotBlank(message = "Falta API_PUBLIC_URL (ver .env.example)") @URL
        String apiPublicUrl) {

    /** URL absoluta de una ruta del web-app, sin barras duplicadas. */
    public String frontend(String path) {
        return frontendUrl.replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
    }
}
