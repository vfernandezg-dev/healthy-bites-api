package com.healthybites.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/** Reloj inyectable (los tests pueden sustituirlo) y tareas programadas (limpieza de sesiones caducadas). */
@Configuration
@EnableScheduling
public class TimeConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
