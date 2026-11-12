package com.healthybites.identity.app;

import com.healthybites.identity.domain.PasswordResetToken;
import com.healthybites.identity.domain.PasswordResetTokenRepository;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import com.healthybites.shared.config.AppProperties;
import com.healthybites.shared.security.OpaqueTokens;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Restablecimiento de contraseña.
 *
 * <ul>
 *   <li>La solicitud responde igual exista o no la cuenta y el correo sale después del commit en otro hilo, así que
 *       ni la respuesta ni su tiempo revelan qué correos están registrados.</li>
 *   <li>El enlace lleva el token en el fragmento ({@code #token=}), que el navegador no envía al servidor: no queda
 *       en logs de acceso ni en la cabecera {@code Referer}.</li>
 *   <li>Solo se guarda el hash del token; caduca a los 30 minutos y sirve una vez.</li>
 *   <li>Cambiar la contraseña cierra todas las sesiones abiertas.</li>
 * </ul>
 */
@Slf4j
@Service
public class PasswordResetService {

    static final String INVALID_LINK = "El enlace no es válido o ha caducado. Solicita uno nuevo.";

    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokens;
    private final AuthRateLimiter rateLimiter;
    private final TransactionOperations transactions;
    private final ApplicationEventPublisher events;
    private final AppProperties appProperties;
    private final Clock clock;
    private final Duration tokenTtl;

    public PasswordResetService(
            PasswordResetTokenRepository tokens,
            UserRepository users,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokens,
            AuthRateLimiter rateLimiter,
            TransactionOperations transactions,
            ApplicationEventPublisher events,
            AppProperties appProperties,
            Clock clock,
            AuthProperties properties) {
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokens = refreshTokens;
        this.rateLimiter = rateLimiter;
        this.transactions = transactions;
        this.events = events;
        this.appProperties = appProperties;
        this.clock = clock;
        this.tokenTtl = properties.passwordReset().tokenTtl();
    }

    /**
     * Si la cuenta existe y está activa, crea un enlace nuevo (invalida los anteriores) y lo envía por correo. El
     * límite se comprueba antes de abrir la transacción (ver {@code RateLimiter}).
     */
    public void requestReset(String email, String clientIp) {
        rateLimiter.checkPasswordReset(clientIp, email);
        transactions.executeWithoutResult(status -> users.findByEmail(email.trim())
                .filter(User::isActive)
                .ifPresentOrElse(
                        this::issueLink,
                        () -> log.debug("Restablecimiento pedido para una cuenta inexistente o inactiva")));
    }

    /** Cambia la contraseña con un enlace válido, lo consume y cierra todas las sesiones del usuario. */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken resetToken = tokens.findByTokenHash(OpaqueTokens.hash(token))
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> ApiException.badRequest("invalid-reset-link", INVALID_LINK));

        User user = resetToken.getUser();
        user.changePassword(Objects.requireNonNull(passwordEncoder.encode(newPassword)));
        UUID userId = Objects.requireNonNull(user.getId());

        resetToken.markUsed(now);
        tokens.deleteAllOfUser(userId);
        refreshTokens.revokeAllSessions(userId);
        log.info("Contraseña restablecida para el usuario {}", userId);
    }

    @Scheduled(cron = "0 45 4 * * *")
    @SchedulerLock(name = "password-reset-cleanup", lockAtMostFor = "PT10M")
    @Transactional
    public void purgeExpired() {
        int purged = tokens.deleteExpiredBefore(clock.instant());
        if (purged > 0) {
            log.info("Enlaces de restablecimiento caducados eliminados: {}", purged);
        }
    }

    private void issueLink(User user) {
        Instant now = clock.instant();
        String token = OpaqueTokens.generate();
        tokens.deleteAllOfUser(Objects.requireNonNull(user.getId()));
        tokens.save(PasswordResetToken.issue(user, OpaqueTokens.hash(token), now, now.plus(tokenTtl)));
        String link = appProperties.frontend("auth/reset-password") + "#token=" + token;
        events.publishEvent(new PasswordResetRequested(user.getEmail(), user.getFirstName(), link, tokenTtl));
    }
}
