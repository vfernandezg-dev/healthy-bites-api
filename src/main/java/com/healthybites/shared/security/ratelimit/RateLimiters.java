package com.healthybites.shared.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Crea los limitadores de peticiones. Con {@code healthybites.security.rate-limit.store=jdbc} (por defecto) los
 * eventos se cuentan en PostgreSQL y el límite es el mismo para todas las instancias; con {@code memory}, cada
 * instancia cuenta los suyos (solo vale con una instancia).
 */
@Component
public class RateLimiters {

    private final RateLimitProperties properties;
    private final JdbcClient jdbc;
    private final PlatformTransactionManager transactionManager;
    private final Clock clock;

    RateLimiters(
            RateLimitProperties properties,
            JdbcClient jdbc,
            PlatformTransactionManager transactionManager,
            Clock clock) {
        this.properties = properties;
        this.jdbc = jdbc;
        this.transactionManager = transactionManager;
        this.clock = clock;
    }

    /**
     * @param name nombre único del límite (por ejemplo {@code login-ip}): separa sus claves de las de otros límites
     */
    public RateLimiter create(String name, int maxEvents, Duration window) {
        return switch (properties.store()) {
            case JDBC -> new JdbcRateLimiter(name, maxEvents, window, jdbc, transactionManager, clock);
            case MEMORY -> new InMemoryRateLimiter(maxEvents, window, clock);
        };
    }

    @ConfigurationProperties("healthybites.security.rate-limit")
    public record RateLimitProperties(@DefaultValue("jdbc") Store store) {

        public enum Store {
            JDBC,
            MEMORY
        }
    }
}
