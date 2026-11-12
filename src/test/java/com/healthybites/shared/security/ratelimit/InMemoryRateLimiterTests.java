package com.healthybites.shared.security.ratelimit;

import com.healthybites.support.MutableClock;
import java.time.Duration;
import java.time.Instant;

/** El contrato de {@link RateLimiter} sobre la implementación en memoria. */
class InMemoryRateLimiterTests implements RateLimiterContract {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));

    @Override
    public MutableClock clock() {
        return clock;
    }

    @Override
    public RateLimiter limiter(int maxEvents, Duration window) {
        return new InMemoryRateLimiter(maxEvents, window, clock);
    }
}
