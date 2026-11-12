package com.healthybites.identity.app;

import com.healthybites.identity.UserAccount;
import com.healthybites.shared.security.JwtProperties;
import com.healthybites.shared.security.TokenClaims;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Emite los access tokens: JWT HS512 de vida corta (por defecto, 15 minutos) que el web-app guarda solo en memoria.
 * El resource server de Spring Security los valida en cada petición (firma, {@code exp}, {@code iss} y {@code aud}).
 */
@Service
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public AccessTokenService(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    public record AccessToken(String value, Instant expiresAt, long expiresInSeconds) {}

    public AccessToken issue(UserAccount account) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .audience(List.of(properties.audience()))
                .subject(account.id().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(TokenClaims.ROLES, List.of(account.role().name()))
                .claim(TokenClaims.EMAIL, account.email())
                .build();
        // El algoritmo se fija explícitamente en la cabecera: el decoder solo acepta HS512.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).type("JWT").build();
        String value =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(value, expiresAt, properties.accessTokenTtl().toSeconds());
    }
}
