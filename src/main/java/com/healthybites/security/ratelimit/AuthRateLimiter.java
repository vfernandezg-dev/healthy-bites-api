package com.healthybites.security.ratelimit;

import com.healthybites.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

/**
 * Límites de los endpoints de sesión contra la fuerza bruta.
 *
 * <ul>
 *   <li>Login: intentos <em>fallidos</em> por IP + correo y por IP. Un login correcto limpia el contador de la
 *       cuenta.</li>
 *   <li>Refresh: peticiones por IP.</li>
 * </ul>
 */
@Component
public class AuthRateLimiter {

    private static final String TOO_MANY_LOGINS = "Demasiados intentos de inicio de sesión. Inténtalo más tarde.";
    private static final String TOO_MANY_REFRESHES = "Demasiadas renovaciones de sesión. Inténtalo más tarde.";

    private final SlidingWindowRateLimiter loginFailuresByAccount;
    private final SlidingWindowRateLimiter loginFailuresByIp;
    private final SlidingWindowRateLimiter refreshesByIp;

    public AuthRateLimiter(
            @Value("${healthybites.security.rate-limit.login-per-account:5}") int loginPerAccount,
            @Value("${healthybites.security.rate-limit.login-per-ip:20}") int loginPerIp,
            @Value("${healthybites.security.rate-limit.login-window:PT15M}") Duration loginWindow,
            @Value("${healthybites.security.rate-limit.refresh-per-ip:60}") int refreshPerIp,
            @Value("${healthybites.security.rate-limit.refresh-window:PT5M}") Duration refreshWindow,
            Clock clock) {
        this.loginFailuresByAccount = new SlidingWindowRateLimiter(loginPerAccount, loginWindow, clock);
        this.loginFailuresByIp = new SlidingWindowRateLimiter(loginPerIp, loginWindow, clock);
        this.refreshesByIp = new SlidingWindowRateLimiter(refreshPerIp, refreshWindow, clock);
    }

    /** @throws TooManyRequestsException si la cuenta o la IP acumulan demasiados fallos recientes */
    public void checkLogin(String clientIp, String email) {
        loginFailuresByAccount.retryAfter(accountKey(clientIp, email))
                .or(() -> loginFailuresByIp.retryAfter(clientIp))
                .ifPresent(wait -> {
                    throw new TooManyRequestsException(TOO_MANY_LOGINS, wait);
                });
    }

    public void loginFailed(String clientIp, String email) {
        loginFailuresByAccount.record(accountKey(clientIp, email));
        loginFailuresByIp.record(clientIp);
    }

    public void loginSucceeded(String clientIp, String email) {
        loginFailuresByAccount.reset(accountKey(clientIp, email));
    }

    /** @throws TooManyRequestsException si la IP supera el límite de renovaciones */
    public void checkRefresh(String clientIp) {
        if (!refreshesByIp.tryAcquire(clientIp)) {
            throw new TooManyRequestsException(TOO_MANY_REFRESHES,
                    refreshesByIp.retryAfter(clientIp).orElse(Duration.ZERO));
        }
    }

    private static String accountKey(String clientIp, String email) {
        return clientIp + "|" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }
}
