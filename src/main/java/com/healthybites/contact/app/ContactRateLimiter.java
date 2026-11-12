package com.healthybites.contact.app;

import com.healthybites.shared.security.ratelimit.RateLimiter;
import com.healthybites.shared.security.ratelimit.RateLimiters;
import java.time.Duration;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Límites del formulario de contacto: uno por cliente (IP) y otro global. El global acota el total de correos
 * enviados aunque la IP se falsifique. En memoria: suficiente para una sola instancia.
 */
@Component
public class ContactRateLimiter {

    private static final String UNKNOWN_CLIENT = "desconocido";
    private static final String GLOBAL = "*";

    private final RateLimiter perClient;
    private final RateLimiter global;

    public ContactRateLimiter(ContactProperties properties, RateLimiters rateLimiters) {
        ContactProperties.RateLimit limits = properties.rateLimit();
        this.perClient = rateLimiters.create("contact-client", limits.perClient(), limits.perClientWindow());
        this.global = rateLimiters.create("contact-global", limits.global(), limits.globalWindow());
    }

    /**
     * Registra un intento de envío si hay margen.
     *
     * @return vacío si se registró; si no, el tiempo hasta que el cliente (o el total) vuelva a tener margen
     */
    public synchronized Optional<Duration> acquire(@Nullable String clientKey) {
        String key = (clientKey == null || clientKey.isBlank()) ? UNKNOWN_CLIENT : clientKey;
        Optional<Duration> clientWait = perClient.retryAfter(key);
        Optional<Duration> globalWait = global.retryAfter(GLOBAL);
        if (clientWait.isPresent() || globalWait.isPresent()) {
            return Optional.of(longest(clientWait.orElse(Duration.ZERO), globalWait.orElse(Duration.ZERO)));
        }
        perClient.record(key);
        global.record(GLOBAL);
        return Optional.empty();
    }

    private static Duration longest(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}
