package com.healthybites.contact.web;

import static com.healthybites.shared.security.PublicEndpoints.post;

import com.healthybites.shared.security.PublicEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ContactWebConfiguration {

    @Bean
    PublicEndpoints contactPublicEndpoints() {
        return PublicEndpoints.of(post("/contact"));
    }
}
