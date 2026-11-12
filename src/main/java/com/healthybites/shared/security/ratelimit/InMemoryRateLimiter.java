package com.healthybites.shared.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Limitador en memoria con ventana deslizante. Cada instancia cuenta sus propios eventos, así que solo sirve con una
 * instancia ({@code healthybites.security.rate-limit.store=memory}); con varias se usa {@link JdbcRateLimiter}.
 */
final class InMemoryRateLimiter implements RateLimiter {

    private static final int SWEEP_THRESHOLD = 1_000;

    private final int maxEvents;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> events = new HashMap<>();

    InMemoryRateLimiter(int maxEvents, Duration window, Clock clock) {
        if (maxEvents < 1) {
            throw new IllegalArgumentException("maxEvents debe ser al menos 1");
        }
        this.maxEvents = maxEvents;
        this.window = window;
        this.clock = clock;
    }

    @Override
    public synchronized boolean tryAcquire(String key) {
        if (retryAfter(key).isPresent()) {
            return false;
        }
        record(key);
        return true;
    }

    @Override
    public synchronized Optional<Duration> retryAfter(String key) {
        Instant now = clock.instant();
        Deque<Instant> hits = events.get(key);
        if (hits == null) {
            return Optional.empty();
        }
        prune(hits, now);
        if (hits.size() < maxEvents) {
            return Optional.empty();
        }
        // Hay margen cuando sale de la ventana el evento número maxEvents contando desde el más reciente.
        Instant blocking =
                hits.stream().skip(hits.size() - maxEvents).findFirst().orElseThrow();
        Duration wait = Duration.between(now, blocking.plus(window));
        return Optional.of(wait.isNegative() ? Duration.ZERO : wait);
    }

    @Override
    public synchronized void record(String key) {
        Instant now = clock.instant();
        Deque<Instant> hits = events.computeIfAbsent(key, k -> new ArrayDeque<>());
        prune(hits, now);
        hits.addLast(now);
        if (events.size() > SWEEP_THRESHOLD) {
            events.values().removeIf(deque -> {
                prune(deque, now);
                return deque.isEmpty();
            });
        }
    }

    @Override
    public synchronized void reset(String key) {
        events.remove(key);
    }

    private void prune(Deque<Instant> hits, Instant now) {
        Instant threshold = now.minus(window);
        while (!hits.isEmpty() && !hits.getFirst().isAfter(threshold)) {
            hits.pollFirst();
        }
    }
}
