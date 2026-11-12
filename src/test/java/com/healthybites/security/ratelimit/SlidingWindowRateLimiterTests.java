package com.healthybites.security.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowRateLimiterTests {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));
    private final SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, Duration.ofMinutes(10), clock);

    @Test
    void allowsUpToTheLimitAndThenReportsTheWait() {
        assertThat(limiter.tryAcquire("ip")).isTrue();
        clock.advance(Duration.ofMinutes(2));
        assertThat(limiter.tryAcquire("ip")).isTrue();
        assertThat(limiter.tryAcquire("ip")).isTrue();

        assertThat(limiter.tryAcquire("ip")).isFalse();
        // El primer evento sale de la ventana 10 min después de producirse: faltan 8.
        assertThat(limiter.retryAfter("ip")).contains(Duration.ofMinutes(8));
    }

    @Test
    void theWindowSlides() {
        limiter.tryAcquire("ip");
        limiter.tryAcquire("ip");
        limiter.tryAcquire("ip");

        clock.advance(Duration.ofMinutes(10).plusSeconds(1));

        assertThat(limiter.retryAfter("ip")).isEmpty();
        assertThat(limiter.tryAcquire("ip")).isTrue();
    }

    @Test
    void keysAreIndependentAndCanBeReset() {
        limiter.record("a");
        limiter.record("a");
        limiter.record("a");

        assertThat(limiter.tryAcquire("b")).isTrue();
        assertThat(limiter.retryAfter("a")).isPresent();

        limiter.reset("a");
        assertThat(limiter.retryAfter("a")).isEmpty();
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
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
