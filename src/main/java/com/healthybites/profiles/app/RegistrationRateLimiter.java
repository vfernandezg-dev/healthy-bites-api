package com.healthybites.profiles.app;

import com.healthybites.shared.security.ratelimit.RateLimiter;
import com.healthybites.shared.security.ratelimit.RateLimiters;
import com.healthybites.shared.web.ApiException;
import java.time.Duration;
import org.springframework.stereotype.Component;

/** Altas por IP: frena la creación masiva de cuentas desde un mismo origen. */
@Component
class RegistrationRateLimiter {

    private final RateLimiter perIp;

    RegistrationRateLimiter(RegistrationProperties properties, RateLimiters rateLimiters) {
        this.perIp = rateLimiters.create(
                "registration-ip", properties.registrationsPerIp(), properties.registrationWindow());
    }

    void check(String clientIp) {
        if (!perIp.tryAcquire(clientIp)) {
            throw ApiException.tooManyRequests(
                    "Demasiados registros desde esta conexión. Inténtalo más tarde.",
                    perIp.retryAfter(clientIp).orElse(Duration.ZERO));
        }
    }
}
