package com.healthybites.security.session;

import com.healthybites.config.properties.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Cookie del refresh token: {@code HttpOnly} (JavaScript no puede leerla), {@code Secure}, {@code SameSite=Strict}
 * (no viaja en peticiones iniciadas desde otro sitio) y {@code Path} limitado a los endpoints de sesión.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookies {

    private final RefreshTokenProperties properties;
    private final Clock clock;

    public String read(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, properties.cookieName());
        return cookie == null ? null : cookie.getValue();
    }

    public String create(String value, Instant expiresAt) {
        Duration maxAge = Duration.between(clock.instant(), expiresAt);
        return base(value).maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge).build().toString();
    }

    public String clear() {
        return base("").maxAge(Duration.ZERO).build().toString();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.cookieName(), value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Strict")
                .path(properties.cookiePath());
    }
}
