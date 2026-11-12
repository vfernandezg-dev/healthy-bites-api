package com.healthybites.shared.security;

import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Usuario de la petición actual, tal como lo firma el access token. Es la única fuente fiable de identidad: ningún
 * cuerpo ni ruta de la API lleva el id del propietario.
 */
public final class CurrentUser {

    private static final String ROLE_PREFIX = "ROLE_";

    private CurrentUser() {}

    /** Id del usuario ({@code sub} del token). */
    public static UUID id() {
        return UUID.fromString(jwt().getSubject());
    }

    public static String email() {
        return jwt().getClaimAsString(TokenClaims.EMAIL);
    }

    /** Rol de la cuenta (cada cuenta tiene exactamente uno). */
    public static Role role() {
        return authentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith(ROLE_PREFIX))
                .map(authority -> Role.valueOf(authority.substring(ROLE_PREFIX.length())))
                .findFirst()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("La sesión no tiene rol"));
    }

    public static boolean hasRole(Role role) {
        return authentication().getAuthorities().stream()
                .anyMatch(authority -> (ROLE_PREFIX + role.name()).equals(authority.getAuthority()));
    }

    /** NUTRITIONIST o ADMIN: ven sus borradores y el contenido sin restricción de nivel. */
    public static boolean isStaff() {
        return hasRole(Role.NUTRITIONIST) || hasRole(Role.ADMIN);
    }

    private static Jwt jwt() {
        if (authentication() instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
    }

    private static Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado");
        }
        return authentication;
    }
}
