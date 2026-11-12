package com.healthybites.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.identity.app.PasswordResetRequested;
import com.healthybites.shared.security.OpaqueTokens;
import com.healthybites.support.Seed;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Sesión: access token corto en el cuerpo + refresh token rotatorio en una cookie httpOnly, con detección de
 * reutilización, defensa CSRF y límites. Y el restablecimiento de contraseña por enlace de un solo uso.
 */
@RecordApplicationEvents
class AuthenticationTests extends IntegrationTest {

    private static final String APP_ORIGIN = "http://localhost:4210";
    private static final String LANDING_ORIGIN = "http://localhost:4321";
    private static final String NEW_PASSWORD = "NuevaClave-2026";

    @Value("${healthybites.security.refresh.cookie-name}")
    private String cookieName;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEvents events;

    // ---------------------------------------------------------------- login

    @Test
    void loginReturnsAShortLivedAccessTokenTheUserAndAHardenedRefreshCookie() throws Exception {
        MockHttpServletResponse response = attemptLogin(Seed.JUAN, Seed.PASSWORD)
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.id").value(Seed.JUAN_ID.toString()))
                .andExpect(jsonPath("$.user.role").value("CLIENT"))
                .andExpect(jsonPath("$.user.firstName").value("Juan"))
                .andReturn()
                .getResponse();

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .startsWith(cookieName + "=")
                .contains("HttpOnly", "SameSite=Strict", "Path=/api/v1/auth");
    }

    @Test
    void theAccessTokenOpensTheApiAndAWrongPasswordIsA401WithoutDetails() throws Exception {
        attemptLogin(Seed.JUAN, "contrasena-incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("bad-credentials"));
        attemptLogin("nadie@example.com", Seed.PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
    }

    @Test
    void emailsAreCaseInsensitive() throws Exception {
        attemptLogin("Juan.Perez@HealthyBites.com", Seed.PASSWORD).andExpect(status().isOk());
    }

    @Test
    void aDeactivatedAccountCannotLogIn() throws Exception {
        UUID id = createUser("desactivada");
        jdbc.update("update users set status = 'DEACTIVATED' where id = ?", id);

        attemptLogin(emailOf(id), Seed.PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailuresAreRateLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            failedLoginFrom("198.51.100.77").andExpect(status().isUnauthorized());
        }
        failedLoginFrom("198.51.100.77")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    // ---------------------------------------------------------------- refresh y logout

    @Test
    void refreshRotatesTheCookieAndReusingAnOldOneAfterTheGraceWindowEndsTheSession() throws Exception {
        String first =
                cookieValue(attemptLogin(Seed.MARIA, Seed.PASSWORD).andReturn().getResponse());

        String second = cookieValue(mockMvc.perform(refresh(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse());
        assertThat(second).isNotEqualTo(first);

        // Reutilizar el primero al momento solo se rechaza (dos pestañas refrescando a la vez)...
        mockMvc.perform(refresh(first))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("session-expired"));
        mockMvc.perform(refresh(second)).andExpect(status().isOk());

        // ...pero fuera de la ventana de gracia revoca la sesión entera.
        jdbc.update(
                "update refresh_tokens set revoked_at = revoked_at - interval '1 minute' where token_hash = ?",
                OpaqueTokens.hash(first));
        mockMvc.perform(refresh(first)).andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject(
                        "select count(*) from refresh_tokens where family_id = (select family_id from refresh_tokens"
                                + " where token_hash = ?) and revoked_at is null",
                        Integer.class,
                        OpaqueTokens.hash(first)))
                .isZero();
    }

    @Test
    void refreshRequiresTheCustomHeaderAndAnAllowedOrigin() throws Exception {
        String cookie =
                cookieValue(attemptLogin(Seed.PEDRO, Seed.PASSWORD).andReturn().getResponse());

        mockMvc.perform(post("/auth/refresh").cookie(new Cookie(cookieName, cookie)))
                .andExpect(status().isForbidden());
        mockMvc.perform(refresh(cookie).header(HttpHeaders.ORIGIN, "https://evil.example"))
                .andExpect(status().isForbidden());
        mockMvc.perform(refresh(cookie).header(HttpHeaders.ORIGIN, APP_ORIGIN)).andExpect(status().isOk());
    }

    @Test
    void logoutRevokesTheSessionAndClearsTheCookie() throws Exception {
        String cookie =
                cookieValue(attemptLogin(Seed.ELENA, Seed.PASSWORD).andReturn().getResponse());

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(cookieName, cookie))
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
        mockMvc.perform(refresh(cookie)).andExpect(status().isUnauthorized());
    }

    @Test
    void anExpiredOrForgedBearerIsIgnoredOnSessionEndpoints() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-caducado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(Seed.JUAN, Seed.PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void corsAllowsTheAppWithCredentialsAndTheLandingOnlyOnContact() throws Exception {
        mockMvc.perform(preflight("/auth/refresh", APP_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, APP_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        mockMvc.perform(preflight("/auth/refresh", LANDING_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        mockMvc.perform(preflight("/contact", LANDING_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LANDING_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    // ---------------------------------------------------------------- restablecer la contraseña

    @Test
    void forgotPasswordAnswersTheSameForUnknownEmailsAndSendsNothing() throws Exception {
        forgot("nadie-" + UUID.randomUUID() + "@example.com")
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        assertThat(events.stream(PasswordResetRequested.class)).isEmpty();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void theResetLinkCarriesTheTokenInTheFragmentAndOnlyItsHashIsStored() throws Exception {
        UUID id = createUser("reset-enlace");

        forgot(emailOf(id)).andExpect(status().isAccepted());

        PasswordResetRequested event = singleResetEvent();
        assertThat(event.link()).startsWith("http://localhost:4210/auth/reset-password#token=");
        assertThat(jdbc.queryForList(
                        "select token_hash from password_reset_tokens where user_id = ?", String.class, id))
                .containsExactly(OpaqueTokens.hash(tokenOf(event)));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void resettingChangesThePasswordEndsEverySessionAndConsumesTheLink() throws Exception {
        UUID id = createUser("reset-ok");
        String email = emailOf(id);
        String cookie =
                cookieValue(attemptLogin(email, Seed.PASSWORD).andReturn().getResponse());
        forgot(email);
        String token = tokenOf(singleResetEvent());

        reset(token, NEW_PASSWORD).andExpect(status().isNoContent());

        attemptLogin(email, Seed.PASSWORD).andExpect(status().isUnauthorized());
        attemptLogin(email, NEW_PASSWORD).andExpect(status().isOk());
        mockMvc.perform(refresh(cookie)).andExpect(status().isUnauthorized());
        reset(token, "OtraClave-2026!")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid-reset-link"));
    }

    @Test
    void anExpiredLinkIsRejectedAndOnlyTheLatestOneWorks() throws Exception {
        UUID id = createUser("reset-caducado");
        forgot(emailOf(id));
        forgot(emailOf(id));
        List<PasswordResetRequested> sent =
                events.stream(PasswordResetRequested.class).toList();
        assertThat(sent).hasSize(2);

        reset(tokenOf(sent.get(0)), NEW_PASSWORD).andExpect(status().isBadRequest());

        jdbc.update(
                "update password_reset_tokens set expires_at = ? where token_hash = ?",
                Timestamp.from(Instant.now().minusSeconds(60)),
                OpaqueTokens.hash(tokenOf(sent.get(1))));
        reset(tokenOf(sent.get(1)), NEW_PASSWORD).andExpect(status().isBadRequest());
    }

    @Test
    void theNewPasswordFollowsThePolicy() throws Exception {
        reset("x".repeat(43), "corta")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("validation-failed"))
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    @Test
    void resetRequestsAreRateLimitedPerAccountEvenForUnknownEmails() throws Exception {
        String email = "limite-" + UUID.randomUUID() + "@example.com";
        for (int i = 0; i < 3; i++) {
            forgot(email).andExpect(status().isAccepted());
        }
        forgot(email).andExpect(status().isTooManyRequests()).andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void anAccessTokenIsRequiredOutsideThePublicEndpoints() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthenticated"));
        mockMvc.perform(get("/admin/users").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
    }

    // ---------------------------------------------------------------- utilidades

    private ResultActions attemptLogin(String email, String password) throws Exception {
        return mockMvc.perform(
                post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody(email, password)));
    }

    private ResultActions failedLoginFrom(String ip) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("objetivo@example.com", "contrasena-incorrecta"))
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }

    private MockHttpServletRequestBuilder refresh(String cookie) {
        return post("/auth/refresh")
                .cookie(new Cookie(cookieName, cookie))
                .header("X-Requested-With", "XMLHttpRequest");
    }

    private ResultActions forgot(String email) throws Exception {
        return mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s"}
                        """.formatted(email)));
    }

    private ResultActions reset(String token, String password) throws Exception {
        return mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token": "%s", "password": "%s"}
                        """.formatted(token, password)));
    }

    private static MockHttpServletRequestBuilder preflight(String path, String origin) {
        return options(path)
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type");
    }

    private String cookieValue(MockHttpServletResponse response) {
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull().startsWith(cookieName + "=");
        return setCookie.substring(cookieName.length() + 1, setCookie.indexOf(';'));
    }

    private PasswordResetRequested singleResetEvent() {
        List<PasswordResetRequested> sent =
                events.stream(PasswordResetRequested.class).toList();
        assertThat(sent).hasSize(1);
        return sent.getFirst();
    }

    private static String tokenOf(PasswordResetRequested event) {
        return event.link().substring(event.link().indexOf("#token=") + "#token=".length());
    }

    /** Cuenta de cliente nueva (para no tocar las del seed), con la contraseña del seed. */
    private UUID createUser(String alias) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                "insert into users (id, email, password_hash, role, first_name, last_name) values (?, ?, ?, 'CLIENT', ?, ?)",
                id,
                alias + "-" + id.toString().substring(0, 8) + "@example.com",
                passwordEncoder.encode(Seed.PASSWORD),
                "Prueba",
                alias);
        return id;
    }

    private String emailOf(UUID id) {
        return jdbc.queryForObject("select email from users where id = ?", String.class, id);
    }
}
