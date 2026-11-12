package com.healthybites.identity.web;

import static com.healthybites.shared.security.PublicEndpoints.post;

import com.healthybites.shared.security.PublicEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class IdentityWebConfiguration {

    /** Endpoints de sesión: públicos (el refresh y el logout se autentican con la cookie, no con el bearer). */
    @Bean
    PublicEndpoints identityPublicEndpoints() {
        return PublicEndpoints.of(
                post("/auth/login"),
                post("/auth/refresh"),
                post("/auth/logout"),
                post("/auth/forgot-password"),
                post("/auth/reset-password"));
    }
}
