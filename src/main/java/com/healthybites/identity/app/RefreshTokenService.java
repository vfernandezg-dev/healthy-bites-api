package com.healthybites.identity.app;

import com.healthybites.identity.domain.RefreshToken;
import com.healthybites.identity.domain.RefreshTokenRepository;
import com.healthybites.identity.domain.User;
import com.healthybites.shared.security.OpaqueTokens;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final RefreshTokenProperties properties;
    private final Clock clock;

    public RefreshTokenService(RefreshTokenRepository repository, RefreshTokenProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    /** Valor en claro del token (solo viaja en la cookie) y el usuario de la sesión. */
    public record IssuedRefreshToken(String value, User user, Instant expiresAt) {}

    /** Abre una sesión nueva (familia nueva). */
    @Transactional
    public IssuedRefreshToken issue(User user) {
        Instant now = clock.instant();
        return create(user, UUID.randomUUID(), now, now.plus(properties.absoluteTtl()))
                .issued();
    }

    /**
     * Cambia un token válido por uno nuevo de la misma sesión.
     *
     * @throws InvalidRefreshTokenException si el token no existe, caducó, fue revocado o se reutilizó; en este último
     *     caso, fuera de la ventana de gracia, la sesión entera queda revocada
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public IssuedRefreshToken rotate(@Nullable String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("sin refresh token");
        }
        RefreshToken current = repository
                .findByTokenHash(OpaqueTokens.hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("refresh token desconocido"));
        Instant now = clock.instant();
        Instant revokedAt = current.getRevokedAt();
        if (revokedAt != null) {
            if (current.wasRotated() && now.isBefore(revokedAt.plus(properties.reuseGrace()))) {
                throw new InvalidRefreshTokenException("refresh token recién rotado (refresco concurrente)");
            }
            int revoked = repository.revokeFamily(current.getFamilyId(), now);
            log.warn(
                    "Reutilización de un refresh token revocado: sesión {} revocada ({} tokens) para el usuario {}",
                    current.getFamilyId(),
                    revoked,
                    current.getUser().getId());
            throw new InvalidRefreshTokenException("refresh token reutilizado");
        }
        if (current.isExpiredAt(now)) {
            throw new InvalidRefreshTokenException("refresh token caducado");
        }
        if (!current.getUser().isActive()) {
            repository.revokeFamily(current.getFamilyId(), now);
            throw new InvalidRefreshTokenException("cuenta desactivada");
        }
        Created next = create(current.getUser(), current.getFamilyId(), now, current.getFamilyExpiresAt());
        current.rotateTo(next.entity(), now);
        return next.issued();
    }

    /** Cierra la sesión a la que pertenece el token (logout). No falla si el token no existe. */
    @Transactional
    public void revokeSession(@Nullable String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository
                .findByTokenHash(OpaqueTokens.hash(rawToken))
                .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), clock.instant()));
    }

    /** Cierra todas las sesiones de un usuario (cambio de contraseña, desactivación). */
    @Transactional
    public void revokeAllSessions(UUID userId) {
        repository.revokeAllForUser(userId, clock.instant());
    }

    /** Limpieza diaria de sesiones caducadas hace más de un día. */
    @Scheduled(cron = "0 30 4 * * *")
    @SchedulerLock(name = "refresh-token-cleanup", lockAtMostFor = "PT10M")
    @Transactional
    public void purgeExpired() {
        int deleted = repository.deleteExpiredBefore(clock.instant().minus(Duration.ofDays(1)));
        if (deleted > 0) {
            log.info("Eliminados {} refresh tokens caducados", deleted);
        }
    }

    private record Created(RefreshToken entity, IssuedRefreshToken issued) {}

    private Created create(User user, UUID familyId, Instant now, Instant familyExpiresAt) {
        String raw = OpaqueTokens.generate();
        Instant idleExpiry = now.plus(properties.idleTtl());
        Instant expiresAt = idleExpiry.isBefore(familyExpiresAt) ? idleExpiry : familyExpiresAt;
        RefreshToken entity = repository.save(
                RefreshToken.issue(user, familyId, OpaqueTokens.hash(raw), now, expiresAt, familyExpiresAt));
        return new Created(entity, new IssuedRefreshToken(raw, user, expiresAt));
    }
}
