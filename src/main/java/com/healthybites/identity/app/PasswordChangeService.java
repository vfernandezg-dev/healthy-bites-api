package com.healthybites.identity.app;

import com.healthybites.identity.app.SessionService.Session;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import com.healthybites.shared.web.ApiException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cambio de contraseña con la sesión iniciada. Cierra todas las sesiones (también las de otros dispositivos) y abre
 * una nueva para quien hizo el cambio.
 */
@Service
public class PasswordChangeService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokens;
    private final SessionService sessions;

    public PasswordChangeService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokens,
            SessionService sessions) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokens = refreshTokens;
        this.sessions = sessions;
    }

    @Transactional
    public Session change(UUID userId, String currentPassword, String newPassword) {
        User user = users.findById(userId)
                .orElseThrow(() -> ApiException.notFound("user-not-found", "La cuenta no existe"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.unprocessable("wrong-current-password", "La contraseña actual no es correcta")
                    .with(
                            "errors",
                            List.of(Map.of(
                                    "field", "currentPassword", "message", "La contraseña actual no es correcta")));
        }
        user.changePassword(Objects.requireNonNull(passwordEncoder.encode(newPassword)));
        refreshTokens.revokeAllSessions(userId);
        return sessions.openFor(user);
    }
}
