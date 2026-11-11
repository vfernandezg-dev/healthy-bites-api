package com.healthybites.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Limitador en memoria (ventana deslizante) para el formulario de contacto público.
 *
 * Aplica dos límites: uno por cliente (IP) y otro global. El global acota el total de correos
 * enviados aunque la IP se falsifique (la cabecera X-Forwarded-For no es confiable). Al ser en
 * memoria, los contadores se reinician con la aplicación; es suficiente para una sola instancia.
 */
@Component
public class ContactRateLimiter {

    private static final int SWEEP_THRESHOLD = 1_000;
    private static final String UNKNOWN_CLIENT = "desconocido";

    private final int maxPerClient;
    private final Duration clientWindow;
    private final int maxGlobal;
    private final Duration globalWindow;
    private final Clock clock;

    private final Map<String, Deque<Instant>> clientHits = new HashMap<>();
    private final Deque<Instant> globalHits = new ArrayDeque<>();

    @Autowired
    public ContactRateLimiter(
            @Value("${healthybites.contact.rate-limit.per-client:5}") int maxPerClient,
            @Value("${healthybites.contact.rate-limit.per-client-window:PT10M}") Duration clientWindow,
            @Value("${healthybites.contact.rate-limit.global:50}") int maxGlobal,
            @Value("${healthybites.contact.rate-limit.global-window:PT1H}") Duration globalWindow) {
        this(maxPerClient, clientWindow, maxGlobal, globalWindow, Clock.systemUTC());
    }

    ContactRateLimiter(int maxPerClient, Duration clientWindow, int maxGlobal, Duration globalWindow, Clock clock) {
        this.maxPerClient = maxPerClient;
        this.clientWindow = clientWindow;
        this.maxGlobal = maxGlobal;
        this.globalWindow = globalWindow;
        this.clock = clock;
    }

    /**
     * Registra un intento de envío.
     *
     * @return {@code false} si el cliente o el total de envíos ya alcanzaron el límite de su ventana
     */
    public synchronized boolean tryAcquire(String clientKey) {
        String key = (clientKey == null || clientKey.isBlank()) ? UNKNOWN_CLIENT : clientKey;
        Instant now = clock.instant();

        prune(globalHits, now.minus(globalWindow));
        Deque<Instant> hits = clientHits.computeIfAbsent(key, k -> new ArrayDeque<>());
        prune(hits, now.minus(clientWindow));

        if (hits.size() >= maxPerClient || globalHits.size() >= maxGlobal) {
            if (hits.isEmpty()) {
                clientHits.remove(key);
            }
            return false;
        }

        hits.addLast(now);
        globalHits.addLast(now);
        if (clientHits.size() > SWEEP_THRESHOLD) {
            sweep(now);
        }
        return true;
    }

    /** Libera la memoria de clientes cuya ventana ya expiró. */
    private void sweep(Instant now) {
        Instant threshold = now.minus(clientWindow);
        clientHits.values().removeIf(hits -> {
            prune(hits, threshold);
            return hits.isEmpty();
        });
    }

    private static void prune(Deque<Instant> hits, Instant threshold) {
        while (!hits.isEmpty() && !hits.peekFirst().isAfter(threshold)) {
            hits.pollFirst();
        }
    }
}
