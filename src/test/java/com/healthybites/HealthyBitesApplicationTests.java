package com.healthybites;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke tests de extremo a extremo: la aplicación completa sobre un PostgreSQL real con el seed de demo
 * ({@code data-test.sql}). Son la red de seguridad de las actualizaciones de Spring Boot y Java: si alguno
 * falla tras un cambio de versión, el comportamiento observable de la API cambió.
 */
class HealthyBitesApplicationTests extends IntegrationTest {

    @Test
    void loginWithDemoCredentialsReturnsTokenAndRole() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("maria.lopez@healthybites.com", DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("CLIENTE"))
                .andExpect(jsonPath("$.clienteId").value(2));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("maria.lopez@healthybites.com", "contrasena-incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
    }

    @Test
    void registeredClientCanLogIn() throws Exception {
        mockMvc.perform(post("/auth/register/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Lucía",
                                  "apellido": "Ramos",
                                  "correo": "lucia.ramos@example.com",
                                  "contrasena": "healthy123",
                                  "sexo": "Femenino",
                                  "edad": 29,
                                  "altura": 1.65,
                                  "peso": 61.5
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("lucia.ramos@example.com", DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void clientCanListTheirGoals() throws Exception {
        String token = loginAndGetToken("juan.perez@healthybites.com");

        mockMvc.perform(get("/meta/cliente/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void nutritionistCanListTheirMealPlans() throws Exception {
        String token = loginAndGetToken("carlos.garcia@healthybites.com");

        // Carlos (nutricionista 1) es el autor de los planes 1 y 3 del seed.
        mockMvc.perform(get("/plan-alimenticio/nutricionista/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void contactAcceptsTheLandingPayload() throws Exception {
        // Mismo cuerpo que envía la landing (healthy-bites-landing/src/lib/contact.ts).
        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ana",
                                  "apellido": "Torres",
                                  "correo": "ana@correo.com",
                                  "mensaje": "Quisiera más información sobre los planes.",
                                  "website": ""
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(mailSender).send(any(MimeMessage.class));
    }
}
