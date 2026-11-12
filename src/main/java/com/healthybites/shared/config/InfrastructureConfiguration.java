package com.healthybites.shared.config;

import com.healthybites.shared.web.RequestIdFilter;
import java.time.Clock;
import java.util.Optional;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Infraestructura común: reloj inyectable (los tests pueden sustituirlo), auditoría de las entidades con ese mismo
 * reloj, tareas programadas, {@code @Async} (con hilos virtuales, ver {@code spring.threads.virtual.enabled}) y el
 * identificador de petición para correlacionar logs.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableAsync
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class InfrastructureConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    DateTimeProvider auditingDateTimeProvider(Clock clock) {
        return () -> Optional.of(clock.instant());
    }

    /** Antes que la cadena de seguridad, para que también los 401/403 lleven el identificador. */
    @Bean
    FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>(new RequestIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
