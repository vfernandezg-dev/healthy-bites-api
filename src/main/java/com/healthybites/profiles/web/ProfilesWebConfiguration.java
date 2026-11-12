package com.healthybites.profiles.web;

import static com.healthybites.shared.security.PublicEndpoints.post;

import com.healthybites.shared.security.PublicEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ProfilesWebConfiguration {

    @Bean
    PublicEndpoints profilesPublicEndpoints() {
        return PublicEndpoints.of(
                post("/auth/register"), post("/auth/invitations/preview"), post("/auth/invitations/accept"));
    }
}
