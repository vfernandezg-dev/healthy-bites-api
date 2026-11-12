package com.healthybites.identity.app;

import com.healthybites.identity.UserAccount;
import com.healthybites.identity.app.AccessTokenService.AccessToken;
import com.healthybites.identity.app.RefreshTokenService.IssuedRefreshToken;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import java.time.Clock;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sesiones: login, renovación y logout. Cada sesión es un access token corto (en el cuerpo de la respuesta) más un
 * refresh token rotatorio (en una cookie httpOnly).
 */
@Service
public class SessionService {

    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;
    private final AuthRateLimiter rateLimiter;
    private final UserRepository users;
    private final Clock clock;

    public SessionService(
            AuthenticationManager authenticationManager,
            AccessTokenService accessTokens,
            RefreshTokenService refreshTokens,
            AuthRateLimiter rateLimiter,
            UserRepository users,
            Clock clock) {
        this.authenticationManager = authenticationManager;
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
        this.rateLimiter = rateLimiter;
        this.users = users;
        this.clock = clock;
    }

    /** Una sesión abierta: la cuenta, el access token y el refresh token (para la cookie). */
    public record Session(UserAccount user, AccessToken accessToken, IssuedRefreshToken refreshToken) {}

    @Transactional
    public Session login(String email, String password, String clientIp) {
        rateLimiter.checkLogin(clientIp, email);
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, password));
        } catch (AuthenticationException e) {
            rateLimiter.loginFailed(clientIp, email);
            throw e;
        }
        rateLimiter.loginSucceeded(clientIp, email);
        User user =
                users.findByEmail(email.trim()).orElseThrow(() -> new BadCredentialsException("Cuenta no encontrada"));
        user.recordLogin(clock.instant());
        return open(user, refreshTokens.issue(user));
    }

    /** La revocación por reutilización debe confirmarse aunque se lance la excepción. */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Session refresh(@Nullable String refreshToken, String clientIp) {
        rateLimiter.checkRefresh(clientIp);
        IssuedRefreshToken next = refreshTokens.rotate(refreshToken);
        return open(next.user(), next);
    }

    /** Sesión nueva para una cuenta ya autenticada (por ejemplo, tras cambiar la contraseña). */
    @Transactional
    public Session openFor(User user) {
        return open(user, refreshTokens.issue(user));
    }

    @Transactional
    public void logout(@Nullable String refreshToken) {
        refreshTokens.revokeSession(refreshToken);
    }

    private Session open(User user, IssuedRefreshToken refreshToken) {
        UserAccount account = user.toAccount();
        return new Session(account, accessTokens.issue(account), refreshToken);
    }
}
