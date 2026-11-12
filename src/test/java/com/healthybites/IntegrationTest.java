package com.healthybites;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.billing.app.PaymentGateway;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

/**
 * Base de los tests de integración: la aplicación completa sobre un PostgreSQL real (Testcontainers) con las
 * migraciones y el seed de demostración. Todas las subclases comparten el mismo contexto de Spring y el mismo
 * contenedor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, SynchronousAsyncConfiguration.class})
@ActiveProfiles("test")
public abstract class IntegrationTest {

    /** Access tokens por correo: duran 15 minutos y el contexto se comparte, así que basta un login por cuenta. */
    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

    @Autowired
    protected MockMvc mockMvc;

    // Clave JWT aleatoria por ejecución: el repositorio no contiene ninguna, ni siquiera para los tests.
    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        registry.add(
                "healthybites.security.jwt.secret", () -> Base64.getEncoder().encodeToString(key));
    }

    // Sin SMTP real: el correo se construye entero (plantilla incluida) pero no se envía.
    @MockitoBean
    protected JavaMailSender mailSender;

    // La pasarela de pago (PayPal) nunca se llama de verdad: cada test simula sus respuestas.
    @MockitoBean
    protected PaymentGateway payPal;

    @BeforeEach
    void setUpMail() {
        // Un mensaje nuevo por correo: un test puede enviar varios y revisarlos todos al final.
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((Session) null));
    }

    /** Access token de una cuenta del seed (se reutiliza entre tests). */
    protected String token(String email) throws Exception {
        String cached = TOKENS.get(email);
        if (cached != null) {
            return cached;
        }
        String token = login(email, Seed.PASSWORD);
        TOKENS.put(email, token);
        return token;
    }

    /** Login real (sin caché): devuelve el access token. */
    protected String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    /** Cliente nuevo registrado por la API y con sesión (para no tocar los datos del seed). */
    protected NewUser newClient(String alias) throws Exception {
        String email = alias + "-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String created = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "firstName": "Prueba", "lastName": "%s"}
                                """.formatted(email, Seed.PASSWORD, alias)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new NewUser(UUID.fromString(JsonPath.read(created, "$.id")), email, login(email, Seed.PASSWORD));
    }

    protected record NewUser(UUID id, String email, String token) {}

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String loginBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }
}
