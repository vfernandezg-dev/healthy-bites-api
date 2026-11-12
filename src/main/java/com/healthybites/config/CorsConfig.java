package com.healthybites.config;

import com.healthybites.config.properties.CorsProperties;
import com.healthybites.security.session.CookieRequestGuard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * CORS por listas blancas (sustituye al antiguo filtro con {@code Access-Control-Allow-Origin: *}).
 *
 * <ul>
 *   <li>La landing solo puede llamar a {@code POST /contact}, sin credenciales.</li>
 *   <li>El web-app puede llamar a toda la API con credenciales (la cookie del refresh token).</li>
 * </ul>
 */
@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties cors) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Se registra antes que /**: gana la primera coincidencia.
        if (!cors.landingOrigins().isEmpty()) {
            CorsConfiguration landing = new CorsConfiguration();
            landing.setAllowedOrigins(cors.landingOrigins());
            landing.setAllowedMethods(List.of("POST"));
            landing.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE));
            landing.setExposedHeaders(List.of(HttpHeaders.RETRY_AFTER));
            landing.setMaxAge(Duration.ofHours(1));
            source.registerCorsConfiguration("/contact", landing);
        }

        if (!cors.appOrigins().isEmpty()) {
            CorsConfiguration app = new CorsConfiguration();
            app.setAllowedOrigins(cors.appOrigins());
            app.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
            app.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE,
                    CookieRequestGuard.REQUESTED_WITH));
            app.setExposedHeaders(List.of(HttpHeaders.RETRY_AFTER, HttpHeaders.LOCATION));
            app.setAllowCredentials(true);
            app.setMaxAge(Duration.ofHours(1));
            source.registerCorsConfiguration("/**", app);
        }
        return source;
    }
}
