package com.healthybites.media.web;

import static com.healthybites.shared.security.PublicEndpoints.get;

import com.healthybites.shared.security.PublicEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class MediaWebConfiguration {

    /** Lectura sin token: {@code <img>} y {@code <video>} no pueden enviarlo. Los protegidos exigen URL firmada. */
    @Bean
    PublicEndpoints mediaPublicEndpoints() {
        return PublicEndpoints.of(get("/media/{id}"));
    }
}
