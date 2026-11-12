package com.healthybites.config;

import com.healthybites.config.properties.AppProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Documentación OpenAPI. Swagger UI solo se expone en el perfil dev (ver application-dev.yml).
 */
@Configuration
public class SwaggerAPIConfig {

    @Bean
    public OpenAPI healthyBitesOpenAPI(AppProperties app,
                                       @Value("${healthybites.openapi.contact-email}") String contactEmail) {
        Contact contact = new Contact()
                .name("Healthy Bites")
                .email(contactEmail)
                .url("https://healthybites.vfernandezg.dev");

        Info info = new Info()
                .title("Healthy Bites API")
                .version("1.0")
                .description("API REST de Healthy Bites: nutrición, hábitos, contenido y comunidad.")
                .contact(contact);

        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(info)
                .servers(List.of(new Server().url(app.apiPublicUrl())))
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
