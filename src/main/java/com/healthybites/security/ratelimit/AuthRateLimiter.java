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
    private static final String TOO_MANY_RESETS = "Demasiadas solicitudes de restablecimiento. Inténtalo más tarde.";

    private final SlidingWindowRateLimiter loginFailuresByAccount;
    private final SlidingWindowRateLimiter loginFailuresByIp;
    private final SlidingWindowRateLimiter refreshesByIp;
    private final SlidingWindowRateLimiter passwordResetsByAccount;
    private final SlidingWindowRateLimiter passwordResetsByIp;

    public AuthRateLimiter(
            @Value("${healthybites.security.rate-limit.login-per-account:5}") int loginPerAccount,
            @Value("${healthybites.security.rate-limit.login-per-ip:20}") int loginPerIp,
            @Value("${healthybites.security.rate-limit.login-window:PT15M}") Duration loginWindow,
            @Value("${healthybites.security.rate-limit.refresh-per-ip:60}") int refreshPerIp,
            @Value("${healthybites.security.rate-limit.refresh-window:PT5M}") Duration refreshWindow,
            @Value("${healthybites.security.rate-limit.password-reset-per-account:3}") int passwordResetPerAccount,
            @Value("${healthybites.security.rate-limit.password-reset-per-ip:10}") int passwordResetPerIp,
            @Value("${healthybites.security.rate-limit.password-reset-window:PT1H}") Duration passwordResetWindow,
            Clock clock) {
        this.loginFailuresByAccount = new SlidingWindowRateLimiter(loginPerAccount, loginWindow, clock);
        this.loginFailuresByIp = new SlidingWindowRateLimiter(loginPerIp, loginWindow, clock);
        this.refreshesByIp = new SlidingWindowRateLimiter(refreshPerIp, refreshWindow, clock);
        this.passwordResetsByAccount = new SlidingWindowRateLimiter(passwordResetPerAccount, passwordResetWindow, clock);
        this.passwordResetsByIp = new SlidingWindowRateLimiter(passwordResetPerIp, passwordResetWindow, clock);
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

    /**
     * Cada solicitud cuenta, exista o no la cuenta: frena el envío masivo de correos a una víctima y el barrido de
     * direcciones desde una IP.
     *
     * @throws TooManyRequestsException si la cuenta o la IP superan el límite
     */
    public void checkPasswordReset(String clientIp, String email) {
        String account = normalize(email);
        if (!passwordResetsByIp.tryAcquire(clientIp)) {
            throw new TooManyRequestsException(TOO_MANY_RESETS,
                    passwordResetsByIp.retryAfter(clientIp).orElse(Duration.ZERO));
        }
        if (!passwordResetsByAccount.tryAcquire(account)) {
            throw new TooManyRequestsException(TOO_MANY_RESETS,
                    passwordResetsByAccount.retryAfter(account).orElse(Duration.ZERO));
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String accountKey(String clientIp, String email) {
        return clientIp + "|" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }
}
