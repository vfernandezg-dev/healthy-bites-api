package com.healthybites.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Limitador en memoria con ventana deslizante: como mucho {@code maxEvents} eventos por clave en {@code window}.
 * Suficiente para una sola instancia; con varias réplicas habría que moverlo a un almacén compartido (Redis).
 */
public final class SlidingWindowRateLimiter {

    private static final int SWEEP_THRESHOLD = 1_000;

    private final int maxEvents;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> events = new HashMap<>();

    public SlidingWindowRateLimiter(int maxEvents, Duration window, Clock clock) {
        if (maxEvents < 1) {
            throw new IllegalArgumentException("maxEvents debe ser al menos 1");
        }
        this.maxEvents = maxEvents;
        this.window = window;
        this.clock = clock;
    }

    /** Registra un evento si la clave no alcanzó el límite. */
    public synchronized boolean tryAcquire(String key) {
        if (retryAfter(key).isPresent()) {
            return false;
        }
        record(key);
        return true;
    }

    /** Tiempo hasta que la clave vuelva a tener margen, o vacío si no está limitada. No registra nada. */
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
        Duration wait = Duration.between(now, hits.peekFirst().plus(window));
        return Optional.of(wait.isNegative() ? Duration.ZERO : wait);
    }

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

    public synchronized void reset(String key) {
        events.remove(key);
    }

    private void prune(Deque<Instant> hits, Instant now) {
        Instant threshold = now.minus(window);
        while (!hits.isEmpty() && !hits.peekFirst().isAfter(threshold)) {
            hits.pollFirst();
        }
    }
}
