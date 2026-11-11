package com.healthybites.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ContactRateLimiterTest {

    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-11T12:00:00Z"));
    }

    @Test
    void permiteHastaElLimitePorClienteYLuegoRechaza() {
        ContactRateLimiter limiter = new ContactRateLimiter(3, Duration.ofMinutes(10), 100, Duration.ofHours(1), clock);

        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).isFalse();

        // Otro cliente no se ve afectado.
        assertThat(limiter.tryAcquire("2.2.2.2")).isTrue();
    }

    @Test
    void liberaCupoCuandoExpiraLaVentanaDelCliente() {
        ContactRateLimiter limiter = new ContactRateLimiter(1, Duration.ofMinutes(10), 100, Duration.ofHours(1), clock);

        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        clock.advance(Duration.ofMinutes(9));
        assertThat(limiter.tryAcquire("1.1.1.1")).isFalse();
        clock.advance(Duration.ofMinutes(2));
        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
    }

    @Test
    void elLimiteGlobalAplicaAunqueCambieLaIp() {
        ContactRateLimiter limiter = new ContactRateLimiter(5, Duration.ofMinutes(10), 2, Duration.ofHours(1), clock);

        assertThat(limiter.tryAcquire("1.1.1.1")).isTrue();
        assertThat(limiter.tryAcquire("2.2.2.2")).isTrue();
        assertThat(limiter.tryAcquire("3.3.3.3")).isFalse();

        clock.advance(Duration.ofHours(1).plusSeconds(1));
        assertThat(limiter.tryAcquire("3.3.3.3")).isTrue();
    }

    @Test
    void agrupaClientesSinIdentificar() {
        ContactRateLimiter limiter = new ContactRateLimiter(1, Duration.ofMinutes(10), 100, Duration.ofHours(1), clock);

        assertThat(limiter.tryAcquire(null)).isTrue();
        assertThat(limiter.tryAcquire(" ")).isFalse();
    }

    /** Reloj controlable para simular el paso del tiempo. */
    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
