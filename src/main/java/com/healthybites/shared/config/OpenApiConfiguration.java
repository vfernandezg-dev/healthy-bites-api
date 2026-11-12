package com.healthybites.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Descripción OpenAPI de la API. El contrato se exporta a {@code docs/openapi.yaml} en los tests y el web-app genera
 * sus tipos a partir de él.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    static final String BEARER = "bearer";

    @Bean
    OpenAPI healthyBitesOpenApi(
            AppProperties appProperties, @Value("${healthybites.openapi.contact-email}") String contactEmail) {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthy Bites API")
                        .version("v1")
                        .description("API de Healthy Bites: seguimiento de hábitos y metas, planes de alimentación,"
                                + " contenido, recompensas y comunidad. Errores en formato RFC 9457 (ProblemDetail).")
                        .contact(new Contact().name("Healthy Bites").email(contactEmail))
                        .license(new License().name("Uso privado")))
                .servers(List.of(new Server().url(appProperties.apiPublicUrl())))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Access token de POST /auth/login o /auth/refresh")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
