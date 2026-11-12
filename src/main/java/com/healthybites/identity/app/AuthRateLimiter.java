package com.healthybites.identity.app;

import com.healthybites.shared.security.ratelimit.RateLimiter;
import com.healthybites.shared.security.ratelimit.RateLimiters;
import com.healthybites.shared.web.ApiException;
import java.time.Duration;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Límites de los endpoints de sesión: intentos fallidos de login (por cuenta e IP), renovaciones por IP y
 * solicitudes de restablecimiento de contraseña (por cuenta e IP). Responden 429 con {@code Retry-After}.
 */
@Component
public class AuthRateLimiter {

    private static final String TOO_MANY_LOGINS = "Demasiados intentos de inicio de sesión. Inténtalo más tarde.";
    private static final String TOO_MANY_REFRESHES = "Demasiadas renovaciones de sesión. Inténtalo más tarde.";
    private static final String TOO_MANY_RESETS = "Demasiadas solicitudes de restablecimiento. Inténtalo más tarde.";

    private final RateLimiter loginFailuresByAccount;
    private final RateLimiter loginFailuresByIp;
    private final RateLimiter refreshesByIp;
    private final RateLimiter passwordResetsByAccount;
    private final RateLimiter passwordResetsByIp;

    public AuthRateLimiter(AuthProperties properties, RateLimiters rateLimiters) {
        AuthProperties.RateLimit limits = properties.rateLimit();
        this.loginFailuresByAccount =
                rateLimiters.create("login-account", limits.loginPerAccount(), limits.loginWindow());
        this.loginFailuresByIp = rateLimiters.create("login-ip", limits.loginPerIp(), limits.loginWindow());
        this.refreshesByIp = rateLimiters.create("refresh-ip", limits.refreshPerIp(), limits.refreshWindow());
        this.passwordResetsByAccount = rateLimiters.create(
                "password-reset-account", limits.passwordResetPerAccount(), limits.passwordResetWindow());
        this.passwordResetsByIp =
                rateLimiters.create("password-reset-ip", limits.passwordResetPerIp(), limits.passwordResetWindow());
    }

    /** @throws ApiException 429 si la cuenta o la IP acumulan demasiados fallos recientes */
    public void checkLogin(String clientIp, String email) {
        loginFailuresByAccount
                .retryAfter(accountKey(clientIp, email))
                .or(() -> loginFailuresByIp.retryAfter(clientIp))
                .ifPresent(wait -> {
                    throw ApiException.tooManyRequests(TOO_MANY_LOGINS, wait);
                });
    }

    public void loginFailed(String clientIp, String email) {
        loginFailuresByAccount.record(accountKey(clientIp, email));
        loginFailuresByIp.record(clientIp);
    }

    public void loginSucceeded(String clientIp, String email) {
        loginFailuresByAccount.reset(accountKey(clientIp, email));
    }

    /** @throws ApiException 429 si la IP supera el límite de renovaciones */
    public void checkRefresh(String clientIp) {
        if (!refreshesByIp.tryAcquire(clientIp)) {
            throw ApiException.tooManyRequests(
                    TOO_MANY_REFRESHES, refreshesByIp.retryAfter(clientIp).orElse(Duration.ZERO));
        }
    }

    /**
     * Cada solicitud cuenta, exista o no la cuenta: frena el envío masivo de correos a una víctima y el barrido de
     * direcciones desde una IP.
     *
     * @throws ApiException 429 si la cuenta o la IP superan el límite
     */
    public void checkPasswordReset(String clientIp, String email) {
        String account = normalize(email);
        if (!passwordResetsByIp.tryAcquire(clientIp)) {
            throw ApiException.tooManyRequests(
                    TOO_MANY_RESETS, passwordResetsByIp.retryAfter(clientIp).orElse(Duration.ZERO));
        }
        if (!passwordResetsByAccount.tryAcquire(account)) {
            throw ApiException.tooManyRequests(
                    TOO_MANY_RESETS, passwordResetsByAccount.retryAfter(account).orElse(Duration.ZERO));
        }
    }

    private static String normalize(@Nullable String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String accountKey(String clientIp, @Nullable String email) {
        return clientIp + "|" + normalize(email);
    }
}
