package com.healthybites.shared.config;

import com.healthybites.shared.web.RequestIdFilter;
import java.time.Clock;
import java.util.Optional;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Infraestructura común: reloj inyectable (los tests pueden sustituirlo), auditoría de las entidades con ese mismo
 * reloj, tareas programadas, {@code @Async} (con hilos virtuales, ver {@code spring.threads.virtual.enabled}) y el
 * identificador de petición para correlacionar logs.
 *
 * <p>Con varias instancias, cada tarea {@code @Scheduled} lleva {@code @SchedulerLock}: el bloqueo vive en la tabla
 * {@code shedlock} y solo una instancia la ejecuta a la vez. Las tareas además son idempotentes, así que una segunda
 * ejecución justo después (otra instancia con el reloj algo desplazado) no repite nada.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableAsync
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class InfrastructureConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    DateTimeProvider auditingDateTimeProvider(Clock clock) {
        return () -> Optional.of(clock.instant());
    }

    /** Bloqueos en PostgreSQL con la hora de la base de datos (no la de cada instancia). */
    @Bean
    LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build());
    }

    /** Antes que la cadena de seguridad, para que también los 401/403 lleven el identificador. */
    @Bean
    FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>(new RequestIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
