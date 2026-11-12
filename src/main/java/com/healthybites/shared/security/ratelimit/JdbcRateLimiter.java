package com.healthybites.shared.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Límite compartido por todas las instancias: cada evento es una fila de {@code rate_limit_events}.
 *
 * <ul>
 *   <li>{@code tryAcquire} bloquea la clave ({@code pg_advisory_xact_lock}) mientras cuenta y registra: dos
 *       instancias no pueden pasar del límite a la vez.</li>
 *   <li>Las escrituras van en su propia transacción y exigen que no haya otra activa: así un intento fallido cuenta
 *       aunque la operación de quien llama se deshaga, y nunca se retienen dos conexiones del pool a la vez.</li>
 *   <li>Cada escritura borra los eventos de la clave que ya salieron de la ventana; {@link RateLimitCleanupJob}
 *       borra los de claves que no se vuelven a usar.</li>
 * </ul>
 */
final class JdbcRateLimiter implements RateLimiter {

    private final String name;
    private final int maxEvents;
    private final Duration window;
    private final JdbcClient jdbc;
    private final TransactionTemplate ownTransaction;
    private final Clock clock;

    JdbcRateLimiter(
            String name,
            int maxEvents,
            Duration window,
            JdbcClient jdbc,
            PlatformTransactionManager transactionManager,
            Clock clock) {
        if (maxEvents < 1) {
            throw new IllegalArgumentException("maxEvents debe ser al menos 1");
        }
        this.name = name;
        this.maxEvents = maxEvents;
        this.window = window;
        this.jdbc = jdbc;
        this.clock = clock;
        this.ownTransaction = new TransactionTemplate(transactionManager);
        this.ownTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public boolean tryAcquire(String key) {
        return Boolean.TRUE.equals(write(() -> {
            jdbc.sql("select count(*) from (select pg_advisory_xact_lock(hashtextextended(:lock, 0))) as locked")
                    .param("lock", name + "|" + key)
                    .query(Long.class)
                    .single();
            Instant now = clock.instant();
            if (waitAt(key, now).isPresent()) {
                return false;
            }
            insert(key, now);
            return true;
        }));
    }

    @Override
    public Optional<Duration> retryAfter(String key) {
        return waitAt(key, clock.instant());
    }

    @Override
    public void record(String key) {
        write(() -> {
            insert(key, clock.instant());
            return true;
        });
    }

    @Override
    public void reset(String key) {
        write(() -> jdbc.sql("delete from rate_limit_events where limiter = :limiter and key = :key")
                .param("limiter", name)
                .param("key", key)
                .update());
    }

    /**
     * La clave tiene margen cuando sale de la ventana el evento que hace el número {@code maxEvents} contando desde el
     * más reciente (con exactamente {@code maxEvents} eventos, el más antiguo).
     */
    private Optional<Duration> waitAt(String key, Instant now) {
        return jdbc.sql("""
                        select occurred_at from rate_limit_events
                        where limiter = :limiter and key = :key and occurred_at > :since
                        order by occurred_at desc
                        offset :offset limit 1""")
                .param("limiter", name)
                .param("key", key)
                .param("since", utc(now.minus(window)))
                .param("offset", maxEvents - 1)
                .query((rs, row) -> rs.getObject(1, OffsetDateTime.class).toInstant())
                .optional()
                .map(blocking -> {
                    Duration wait = Duration.between(now, blocking.plus(window));
                    return wait.isNegative() ? Duration.ZERO : wait;
                });
    }

    private void insert(String key, Instant now) {
        jdbc.sql("delete from rate_limit_events where limiter = :limiter and key = :key and occurred_at <= :since")
                .param("limiter", name)
                .param("key", key)
                .param("since", utc(now.minus(window)))
                .update();
        jdbc.sql("insert into rate_limit_events (limiter, key, occurred_at) values (:limiter, :key, :at)")
                .param("limiter", name)
                .param("key", key)
                .param("at", utc(now))
                .update();
    }

    private <T> T write(Supplier<T> work) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Los límites de peticiones se registran fuera de una transacción (" + name
                    + "): dentro retendrían dos conexiones y el registro se desharía con la transacción");
        }
        return ownTransaction.execute(status -> work.get());
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
