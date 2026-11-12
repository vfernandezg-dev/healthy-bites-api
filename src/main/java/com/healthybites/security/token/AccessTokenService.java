package com.healthybites.security.token;

import com.healthybites.config.properties.JwtProperties;
import com.healthybites.model.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Emite los access tokens: JWT HS512 de vida corta (por defecto, 15 minutos) que el web-app guarda solo en memoria.
 * El resource server de Spring Security los valida en cada petición (firma, {@code exp}, {@code iss} y {@code aud}).
 */
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    /** Claim con los roles del usuario, sin el prefijo {@code ROLE_}. */
    public static final String ROLES_CLAIM = "roles";
    public static final String EMAIL_CLAIM = "email";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public record AccessToken(String value, Instant expiresAt, long expiresInSeconds) {
    }

    public AccessToken issue(Usuario usuario) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .audience(List.of(properties.audience()))
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(ROLES_CLAIM, List.of(usuario.getRol().getNombre().name()))
                .claim(EMAIL_CLAIM, usuario.getCorreo())
                .build();
        // El algoritmo se fija explícitamente en la cabecera: el decoder solo acepta HS512.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).type("JWT").build();

        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(value, expiresAt, properties.accessTokenTtl().toSeconds());
    }
}
