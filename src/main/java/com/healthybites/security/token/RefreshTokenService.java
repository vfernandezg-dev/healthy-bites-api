package com.healthybites.security.token;

import com.healthybites.config.properties.RefreshTokenProperties;
import com.healthybites.model.entity.RefreshToken;
import com.healthybites.model.entity.Usuario;
import com.healthybites.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Refresh tokens rotatorios con detección de reutilización.
 *
 * <ul>
 *   <li>El valor es aleatorio (256 bits) y solo se guarda su SHA-256.</li>
 *   <li>Cada uso lo rota: se revoca y se emite otro en la misma familia (la sesión). La sesión caduca si no se usa
 *       ({@code idleTtl}) y, en cualquier caso, al llegar a {@code absoluteTtl}.</li>
 *   <li>Si llega un token ya rotado, alguien más lo tiene: se revoca toda la familia. Excepción: dentro de
 *       {@code reuseGrace} tras la rotación (dos pestañas refrescando a la vez) solo se rechaza.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    private final RefreshTokenRepository repository;
    private final RefreshTokenProperties properties;
    private final Clock clock;

    /** Valor en claro del token (solo viaja en la cookie) y el usuario de la sesión. */
    public record IssuedRefreshToken(String value, Usuario usuario, Instant expiresAt) {
    }

    /** Abre una sesión nueva (familia nueva). */
    @Transactional
    public IssuedRefreshToken issue(Usuario usuario) {
        Instant now = clock.instant();
        return create(usuario, UUID.randomUUID(), now, now.plus(properties.absoluteTtl())).issued();
    }

    /**
     * Cambia un token válido por uno nuevo de la misma sesión.
     *
     * @throws InvalidRefreshTokenException si el token no existe, caducó, fue revocado o se reutilizó; en este último
     *                                      caso, fuera de la ventana de gracia, la sesión entera queda revocada
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public IssuedRefreshToken rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("sin refresh token");
        }
        RefreshToken current = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("refresh token desconocido"));
        Instant now = clock.instant();

        if (current.isRevoked()) {
            if (current.wasRotated() && now.isBefore(current.getRevokedAt().plus(properties.reuseGrace()))) {
                throw new InvalidRefreshTokenException("refresh token recién rotado (refresco concurrente)");
            }
            int revoked = repository.revokeFamily(current.getFamilyId(), now);
            log.warn("Reutilización de un refresh token revocado: sesión {} revocada ({} tokens) para el usuario {}",
                    current.getFamilyId(), revoked, current.getUsuario().getId());
            throw new InvalidRefreshTokenException("refresh token reutilizado");
        }
        if (current.isExpiredAt(now)) {
            throw new InvalidRefreshTokenException("refresh token caducado");
        }

        Created next = create(current.getUsuario(), current.getFamilyId(), now, current.getFamilyExpiresAt());
        current.rotateTo(next.entity(), now);
        return next.issued();
    }

    /** Cierra la sesión a la que pertenece el token (logout). No falla si el token no existe. */
    @Transactional
    public void revokeSession(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), clock.instant()));
    }

    /** Cierra todas las sesiones de un usuario (reset de contraseña, desactivación). */
    @Transactional
    public void revokeAllSessions(Integer userId) {
        repository.revokeAllForUser(userId, clock.instant());
    }

    /** Limpieza diaria de sesiones caducadas hace más de un día. */
    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void purgeExpired() {
        int deleted = repository.deleteExpiredBefore(clock.instant().minus(Duration.ofDays(1)));
        if (deleted > 0) {
            log.info("Eliminados {} refresh tokens caducados", deleted);
        }
    }

    private record Created(RefreshToken entity, IssuedRefreshToken issued) {
    }

    private Created create(Usuario usuario, UUID familyId, Instant now, Instant familyExpiresAt) {
        String raw = newTokenValue();
        Instant idleExpiry = now.plus(properties.idleTtl());
        Instant expiresAt = idleExpiry.isBefore(familyExpiresAt) ? idleExpiry : familyExpiresAt;
        RefreshToken entity = repository.save(
                RefreshToken.issue(usuario, familyId, hash(raw), now, expiresAt, familyExpiresAt));
        return new Created(entity, new IssuedRefreshToken(raw, usuario, expiresAt));
    }

    private static String newTokenValue() {
        return OpaqueTokens.generate();
    }

    static String hash(String raw) {
        return OpaqueTokens.hash(raw);
    }
}
