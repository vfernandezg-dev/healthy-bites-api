package com.healthybites.security.password;

import com.healthybites.config.properties.AppProperties;
import com.healthybites.exception.BadRequestException;
import com.healthybites.model.entity.PasswordResetToken;
import com.healthybites.model.entity.Usuario;
import com.healthybites.repository.PasswordResetTokenRepository;
import com.healthybites.repository.UsuarioRepository;
import com.healthybites.security.ratelimit.AuthRateLimiter;
import com.healthybites.security.token.OpaqueTokens;
import com.healthybites.security.token.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Restablecimiento de contraseña.
 *
 * <ul>
 *   <li>La solicitud responde igual exista o no la cuenta (no revela qué correos están registrados) y el correo se
 *       envía después del commit en otro hilo, así que el tiempo de respuesta tampoco lo delata.</li>
 *   <li>El enlace lleva el token en el fragmento ({@code #token=}), que el navegador no envía al servidor: no queda
 *       en logs de acceso ni en la cabecera {@code Referer}.</li>
 *   <li>Solo se guarda el hash del token; caduca a los 30 minutos y sirve una vez.</li>
 *   <li>Cambiar la contraseña cierra todas las sesiones abiertas.</li>
 * </ul>
 */
@Slf4j
@Service
public class PasswordResetService {

    public static final String INVALID_LINK = "El enlace no es válido o ha caducado. Solicita uno nuevo.";

    private final PasswordResetTokenRepository tokens;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokens;
    private final AuthRateLimiter rateLimiter;
    private final ApplicationEventPublisher events;
    private final AppProperties appProperties;
    private final Clock clock;
    private final Duration tokenTtl;

    public PasswordResetService(PasswordResetTokenRepository tokens, UsuarioRepository usuarios,
                                PasswordEncoder passwordEncoder, RefreshTokenService refreshTokens,
                                AuthRateLimiter rateLimiter, ApplicationEventPublisher events,
                                AppProperties appProperties, Clock clock,
                                @Value("${healthybites.security.password-reset.token-ttl:PT30M}") Duration tokenTtl) {
        this.tokens = tokens;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokens = refreshTokens;
        this.rateLimiter = rateLimiter;
        this.events = events;
        this.appProperties = appProperties;
        this.clock = clock;
        this.tokenTtl = tokenTtl;
    }

    /** Si la cuenta existe, crea un enlace nuevo (invalida los anteriores) y lo envía por correo. */
    @Transactional
    public void requestReset(String email, String clientIp) {
        rateLimiter.checkPasswordReset(clientIp, email);
        usuarios.findByCorreo(email.trim()).ifPresentOrElse(usuario -> {
            Instant now = clock.instant();
            String token = OpaqueTokens.generate();
            tokens.deleteAllOfUser(usuario.getId());
            tokens.save(PasswordResetToken.issue(usuario, OpaqueTokens.hash(token), now, now.plus(tokenTtl)));
            String link = appProperties.frontend("auth/reset-password") + "#token=" + token;
            events.publishEvent(new PasswordResetRequested(usuario.getCorreo(), link, tokenTtl));
        }, () -> log.debug("Restablecimiento pedido para un correo que no está registrado"));
    }

    /** Cambia la contraseña con un enlace válido, lo consume y cierra todas las sesiones del usuario. */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken resetToken = tokens.findByTokenHash(OpaqueTokens.hash(token))
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new BadRequestException(INVALID_LINK));

        Usuario usuario = resetToken.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuarios.save(usuario);

        resetToken.markUsed(now);
        tokens.deleteAllOfUser(usuario.getId());
        refreshTokens.revokeAllSessions(usuario.getId());
        log.info("Contraseña restablecida para el usuario {}", usuario.getId());
    }

    @Scheduled(cron = "0 45 4 * * *")
    @Transactional
    public void purgeExpired() {
        int purged = tokens.deleteExpiredBefore(clock.instant());
        if (purged > 0) {
            log.info("Enlaces de restablecimiento caducados eliminados: {}", purged);
        }
    }
}
