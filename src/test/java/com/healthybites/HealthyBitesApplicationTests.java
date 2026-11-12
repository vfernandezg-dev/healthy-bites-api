package com.healthybites;

import com.jayway.jsonpath.JsonPath;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke tests de extremo a extremo: la aplicación completa sobre un PostgreSQL real con el seed de demo
 * ({@code data-test.sql}). Son la red de seguridad de las actualizaciones de Spring Boot y Java: si alguno
 * falla tras un cambio de versión, el comportamiento observable de la API cambió.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class HealthyBitesApplicationTests {

    private static final String DEMO_PASSWORD = "healthy123";

    @Autowired
    private MockMvc mockMvc;

    // Sin SMTP real: el correo se construye entero (plantilla incluida) pero no se envía.
    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUpMail() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

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
    void loginWithWrongPasswordIsRejected() throws Exception {
        // Hoy responde 500; la fase de seguridad lo corrige a 401. Aquí solo se exige que no sea un éxito.
        int status = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("maria.lopez@healthybites.com", "contrasena-incorrecta")))
                .andReturn().getResponse().getStatus();

        assertThat(status).isGreaterThanOrEqualTo(400);
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

        mockMvc.perform(get("/meta/cliente/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void nutritionistCanListTheirMealPlans() throws Exception {
        String token = loginAndGetToken("carlos.garcia@healthybites.com");

        // Carlos (nutricionista 1) es el autor de los planes 1 y 3 del seed.
        mockMvc.perform(get("/plan-alimenticio/nutricionista/1").header("Authorization", "Bearer " + token))
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

    private String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private static String loginBody(String email, String password) {
        return """
                {"correo": "%s", "contrasena": "%s"}
                """.formatted(email, password);
    }
}
