package com.healthybites.shared.security;

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Rutas públicas que declara cada módulo como bean (por ejemplo, identity sus endpoints de sesión y contact el
 * formulario de la landing). {@link SecurityConfiguration} las abre sin token y, en ellas, ignora la cabecera
 * {@code Authorization}: un access token caducado que el navegador siga enviando no debe impedir el login.
 *
 * <p>El resto de la API exige sesión; {@code /admin/**} exige ADMIN y {@code /nutritionist/**}, NUTRITIONIST.
 */
public record PublicEndpoints(List<Endpoint> endpoints) {

    public static PublicEndpoints of(Endpoint... endpoints) {
        return new PublicEndpoints(Arrays.asList(endpoints));
    }

    public static Endpoint get(String pattern) {
        return new Endpoint(HttpMethod.GET, pattern);
    }

    public static Endpoint post(String pattern) {
        return new Endpoint(HttpMethod.POST, pattern);
    }

    /** Ruta (patrón de {@code PathPattern}, sin el context path) y método; sin método, todos. */
    public record Endpoint(@Nullable HttpMethod method, String pattern) {

        RequestMatcher matcher() {
            return PathPatternRequestMatcher.withDefaults().matcher(method, pattern);
        }
    }
}
