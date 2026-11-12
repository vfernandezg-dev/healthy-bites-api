package com.healthybites.shared.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthybites.IntegrationTest;
import com.healthybites.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** El contrato de {@link RateLimiter} sobre PostgreSQL, y lo que solo tiene sentido con varias instancias. */
class JdbcRateLimiterTests extends IntegrationTest implements RateLimiterContract {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TransactionTemplate transactions;

    @Override
    public MutableClock clock() {
        return clock;
    }

    @Override
    public RateLimiter limiter(int maxEvents, Duration window) {
        return limiter("test-" + UUID.randomUUID(), maxEvents, window);
    }

    private RateLimiter limiter(String name, int maxEvents, Duration window) {
        return new JdbcRateLimiter(name, maxEvents, window, jdbc, transactionManager, clock);
    }

    /** Dos instancias de la API con el mismo límite cuentan los mismos eventos. */
    @Test
    void instancesShareTheirEvents() {
        String name = "shared-" + UUID.randomUUID();
        RateLimiter first = limiter(name, 2, Duration.ofMinutes(10));
        RateLimiter second = limiter(name, 2, Duration.ofMinutes(10));

        assertThat(first.tryAcquire("ip")).isTrue();
        assertThat(second.tryAcquire("ip")).isTrue();

        assertThat(first.tryAcquire("ip")).isFalse();
        assertThat(second.retryAfter("ip")).contains(Duration.ofMinutes(10));
    }

    @Test
    void concurrentAcquisitionsNeverExceedTheLimit() throws Exception {
        RateLimiter limiter = limiter(5, Duration.ofMinutes(10));
        long granted;
        try (ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> attempts = IntStream.range(0, 20)
                    .mapToObj(attempt -> threads.submit(() -> limiter.tryAcquire("ip")))
                    .toList();
            granted = 0;
            for (Future<Boolean> attempt : attempts) {
                if (attempt.get()) {
                    granted++;
                }
            }
        }
        assertThat(granted).isEqualTo(5);
    }

    /** Dentro de una transacción el registro se desharía con ella: es un error de programación y salta en los tests. */
    @Test
    void writingInsideATransactionIsRejected() {
        RateLimiter limiter = limiter(5, Duration.ofMinutes(10));

        assertThatThrownBy(() -> transactions.executeWithoutResult(status -> limiter.record("ip")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fuera de una transacción");
    }

    @Test
    void theCleanupJobDeletesEventsOlderThanADay() {
        String name = "cleanup-" + UUID.randomUUID();
        insertEvent(name, clock.instant().minus(Duration.ofDays(2)));
        insertEvent(name, clock.instant().minus(Duration.ofHours(2)));

        new RateLimitCleanupJob(jdbc, clock).purge();

        assertThat(jdbc.sql("select count(*) from rate_limit_events where limiter = ?")
                        .param(name)
                        .query(Long.class)
                        .single())
                .isEqualTo(1);
    }

    private void insertEvent(String limiter, Instant at) {
        jdbc.sql("insert into rate_limit_events (limiter, key, occurred_at) values (?, 'ip', ?)")
                .params(limiter, at.atOffset(ZoneOffset.UTC))
                .update();
    }
}
