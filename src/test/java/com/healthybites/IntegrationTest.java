package com.healthybites;

import com.jayway.jsonpath.JsonPath;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.SecureRandom;
import java.util.Base64;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de los tests de integración: la aplicación completa sobre un PostgreSQL real (Testcontainers) con el
 * seed de demo. Todas las subclases comparten el mismo contexto de Spring y el mismo contenedor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final String DEMO_PASSWORD = "healthy123";

    @Autowired
    protected MockMvc mockMvc;

    // Clave JWT aleatoria por ejecución: el repositorio no contiene ninguna, ni siquiera para los tests.
    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        registry.add("healthybites.security.jwt.secret", () -> Base64.getEncoder().encodeToString(key));
    }

    // Sin SMTP real: el correo se construye entero (plantilla incluida) pero no se envía.
    @MockitoBean
    protected JavaMailSender mailSender;

    @BeforeEach
    void setUpMail() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    protected String loginAndGetToken(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String loginBody(String email, String password) {
        return """
                {"correo": "%s", "contrasena": "%s"}
                """.formatted(email, password);
    }
}
