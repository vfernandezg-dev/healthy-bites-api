package com.healthybites.identity.web;

import com.healthybites.identity.app.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

/**
 * Cookie del refresh token: {@code HttpOnly} (JavaScript no puede leerla), {@code Secure}, {@code SameSite=Strict}
 * (no viaja en peticiones iniciadas desde otro sitio) y {@code Path} limitado a los endpoints de sesión.
 */
@Component
class RefreshTokenCookies {

    private final RefreshTokenProperties properties;
    private final Clock clock;

    RefreshTokenCookies(RefreshTokenProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Nullable String read(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, properties.cookieName());
        return cookie == null ? null : cookie.getValue();
    }

    String create(String value, Instant expiresAt) {
        Duration maxAge = Duration.between(clock.instant(), expiresAt);
        return base(value)
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .build()
                .toString();
    }

    String clear() {
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
