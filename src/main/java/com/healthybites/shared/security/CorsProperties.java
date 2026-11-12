package com.healthybites.shared.security;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Orígenes permitidos por CORS. Sin orígenes configurados no se permite ninguna petición cruzada.
 *
 * @param appOrigins     orígenes del web-app ({@code CORS_APP_ORIGINS}): toda la API, con credenciales (cookie del
 *                       refresh token)
 * @param landingOrigins orígenes de la landing ({@code CORS_LANDING_ORIGINS}): solo {@code POST /contact}
 */
@ConfigurationProperties("healthybites.cors")
public record CorsProperties(List<String> appOrigins, List<String> landingOrigins) {

    public CorsProperties {
        appOrigins = clean(appOrigins);
        landingOrigins = clean(landingOrigins);
    }

    public boolean isAppOrigin(@Nullable String origin) {
        return origin != null && appOrigins.contains(origin);
    }

    private static List<String> clean(@Nullable List<String> origins) {
        return origins == null
                ? List.of()
                : origins.stream()
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .map(origin -> origin.replaceAll("/+$", ""))
                        .toList();
    }
}
