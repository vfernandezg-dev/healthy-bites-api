package com.healthybites.billing.web;

import static com.healthybites.shared.security.PublicEndpoints.get;

import com.healthybites.shared.security.PublicEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class BillingWebConfiguration {

    /** Los precios se muestran también sin sesión (página de planes). */
    @Bean
    PublicEndpoints billingPublicEndpoints() {
        return PublicEndpoints.of(get("/subscription-plans"));
    }
}
