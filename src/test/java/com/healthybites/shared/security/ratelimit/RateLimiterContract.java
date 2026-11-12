package com.healthybites.shared.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthybites.support.MutableClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Lo que cumple toda implementación de {@link RateLimiter} (en memoria y en PostgreSQL). */
interface RateLimiterContract {

    MutableClock clock();

    /** Un limitador nuevo, sin eventos de otros tests. */
    RateLimiter limiter(int maxEvents, Duration window);

    @Test
    default void allowsUpToTheLimitAndThenReportsTheWait() {
        RateLimiter limiter = limiter(3, Duration.ofMinutes(10));
        assertThat(limiter.tryAcquire("ip")).isTrue();
        clock().advance(Duration.ofMinutes(2));
        assertThat(limiter.tryAcquire("ip")).isTrue();
        assertThat(limiter.tryAcquire("ip")).isTrue();

        assertThat(limiter.tryAcquire("ip")).isFalse();
        // El primer evento sale de la ventana 10 min después de producirse: faltan 8.
        assertThat(limiter.retryAfter("ip")).contains(Duration.ofMinutes(8));
    }

    @Test
    default void theWindowSlides() {
        RateLimiter limiter = limiter(3, Duration.ofMinutes(10));
        limiter.tryAcquire("ip");
        limiter.tryAcquire("ip");
        limiter.tryAcquire("ip");

        clock().advance(Duration.ofMinutes(10).plusSeconds(1));

        assertThat(limiter.retryAfter("ip")).isEmpty();
        assertThat(limiter.tryAcquire("ip")).isTrue();
    }

    @Test
    default void keysAreIndependentAndCanBeReset() {
        RateLimiter limiter = limiter(3, Duration.ofMinutes(10));
        limiter.record("a");
        limiter.record("a");
        limiter.record("a");

        assertThat(limiter.tryAcquire("b")).isTrue();
        assertThat(limiter.retryAfter("a")).isPresent();

        limiter.reset("a");
        assertThat(limiter.retryAfter("a")).isEmpty();
    }

    /** Eventos registrados por encima del límite: la espera es hasta que vuelva a haber menos de {@code maxEvents}. */
    @Test
    default void recordedEventsBeyondTheLimitExtendTheWait() {
        RateLimiter limiter = limiter(2, Duration.ofMinutes(10));
        limiter.record("ip");
        clock().advance(Duration.ofMinutes(1));
        limiter.record("ip");
        clock().advance(Duration.ofMinutes(1));
        limiter.record("ip");

        // Quedan tres en la ventana: hay margen cuando salga el segundo (registrado hace 1 min).
        assertThat(limiter.retryAfter("ip")).contains(Duration.ofMinutes(9));
    }
}
