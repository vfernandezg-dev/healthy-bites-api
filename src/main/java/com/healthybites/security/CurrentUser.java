package com.healthybites.security;

import com.healthybites.security.token.AccessTokenService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Usuario de la petición actual, tal como lo firma el access token. Es la única fuente fiable de identidad: los
 * ids que llegan en la ruta o en el cuerpo los controla el cliente.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** Id del usuario ({@code sub} del token). */
    public static Integer id() {
        return Integer.valueOf(jwt().getSubject());
    }

    public static String email() {
        return jwt().getClaimAsString(AccessTokenService.EMAIL_CLAIM);
    }

    private static Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
    }
}
