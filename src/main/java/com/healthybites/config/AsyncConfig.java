package com.healthybites.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita {@code @Async} (correo de restablecimiento de contraseña). Usa el ejecutor de tareas de Spring Boot,
 * con hilos virtuales si {@code spring.threads.virtual.enabled} está activo.
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class AsyncConfig {
}
