package com.healthybites.security.session;

import com.healthybites.config.properties.CorsProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Defensa CSRF de los endpoints que se autentican con la cookie (refresh y logout), además de SameSite=Strict:
 *
 * <ul>
 *   <li>Exigen la cabecera {@code X-Requested-With}. Un formulario de otro sitio no puede añadirla, y un
 *       {@code fetch} cruzado que la añada necesita un preflight CORS que solo aprueban los orígenes del web-app.</li>
 *   <li>Si el navegador envía {@code Origin}, debe ser uno de los orígenes del web-app: los subdominios hermanos
 *       cuentan como el mismo sitio para SameSite.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CookieRequestGuard {

    public static final String REQUESTED_WITH = "X-Requested-With";

    private final CorsProperties cors;

    public void check(HttpServletRequest request) {
        String requestedWith = request.getHeader(REQUESTED_WITH);
        if (requestedWith == null || requestedWith.isBlank()) {
            throw new AccessDeniedException("Falta la cabecera X-Requested-With");
        }
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin != null && !cors.isAppOrigin(origin)) {
            throw new AccessDeniedException("Origen no permitido: " + origin);
        }
    }
}
