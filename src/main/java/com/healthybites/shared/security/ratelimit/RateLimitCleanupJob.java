package com.healthybites.shared.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Borra cada hora los eventos de límites de peticiones de más de un día (ninguna ventana es tan larga). Las claves que
 * se siguen usando ya se limpian al escribir; esto se ocupa de las que no vuelven (una IP que no regresa).
 */
@Slf4j
@Component
class RateLimitCleanupJob {

    static final Duration RETENTION = Duration.ofDays(1);

    private final JdbcClient jdbc;
    private final Clock clock;

    RateLimitCleanupJob(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Scheduled(cron = "0 20 * * * *")
    @SchedulerLock(name = "rate-limit-cleanup", lockAtMostFor = "PT10M")
    public void purge() {
        int deleted = jdbc.sql("delete from rate_limit_events where occurred_at < :before")
                .param("before", clock.instant().minus(RETENTION).atOffset(ZoneOffset.UTC))
                .update();
        if (deleted > 0) {
            log.debug("Eventos de límites de peticiones caducados borrados: {}", deleted);
        }
    }
}
