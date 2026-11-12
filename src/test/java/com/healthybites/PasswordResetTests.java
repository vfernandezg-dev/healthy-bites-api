package com.healthybites;

import com.healthybites.security.password.PasswordResetRequested;
import com.healthybites.security.token.OpaqueTokens;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Restablecimiento de contraseña (API-8): respuesta idéntica exista o no la cuenta, token solo como hash, de un
 * solo uso y con caducidad, y cambio de contraseña que cierra todas las sesiones.
 */
@RecordApplicationEvents
class PasswordResetTests extends IntegrationTest {

    private static final String NEW_PASSWORD = "NuevaClave-2026";

    @Autowired
    private ApplicationEvents events;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${healthybites.security.refresh.cookie-name}")
    private String cookieName;

    @Test
    void unknownEmailsGetTheSameAnswerAndNoEmail() throws Exception {
        forgot("nadie-" + System.nanoTime() + "@example.com")
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        assertThat(events.stream(PasswordResetRequested.class)).isEmpty();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void theLinkCarriesTheTokenInTheFragmentAndOnlyItsHashIsStored() throws Exception {
        Registered user = registerClient("reset-link");

        forgot(user.email()).andExpect(status().isAccepted());

        PasswordResetRequested event = singleEvent();
        assertThat(event.email()).isEqualTo(user.email());
        assertThat(event.link()).startsWith("http://localhost:4210/auth/reset-password#token=");
        String token = tokenOf(event);
        assertThat(storedHashes(user)).containsExactly(OpaqueTokens.hash(token));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void resettingChangesThePasswordClosesEverySessionAndConsumesTheLink() throws Exception {
        Registered user = registerClient("reset-ok");
        String refreshCookie = refreshCookieOf(login(user.email(), DEMO_PASSWORD).andReturn().getResponse());
        forgot(user.email());
        String token = tokenOf(singleEvent());

        reset(token, NEW_PASSWORD).andExpect(status().isNoContent());

        login(user.email(), DEMO_PASSWORD).andExpect(status().isUnauthorized());
        login(user.email(), NEW_PASSWORD).andExpect(status().isOk());
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(cookieName, refreshCookie))
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized());
        reset(token, "OtraClave-2026!").andExpect(status().isBadRequest());
    }

    @Test
    void anExpiredLinkIsRejected() throws Exception {
        Registered user = registerClient("reset-expired");
        forgot(user.email());
        String token = tokenOf(singleEvent());
        jdbc.update("update password_reset_tokens set expires_at = ? where token_hash = ?",
                Timestamp.from(Instant.now().minusSeconds(60)), OpaqueTokens.hash(token));

        reset(token, NEW_PASSWORD).andExpect(status().isBadRequest());
        login(user.email(), DEMO_PASSWORD).andExpect(status().isOk());
    }

    @Test
    void onlyTheLatestLinkWorks() throws Exception {
        Registered user = registerClient("reset-latest");
        forgot(user.email());
        forgot(user.email());
        List<PasswordResetRequested> sent = events.stream(PasswordResetRequested.class).toList();
        assertThat(sent).hasSize(2);

        reset(tokenOf(sent.get(0)), NEW_PASSWORD).andExpect(status().isBadRequest());
        reset(tokenOf(sent.get(1)), NEW_PASSWORD).andExpect(status().isNoContent());
    }

    @Test
    void theNewPasswordFollowsThePasswordPolicy() throws Exception {
        Registered user = registerClient("reset-policy");
        forgot(user.email());

        reset(tokenOf(singleEvent()), "corta").andExpect(status().isUnprocessableContent());
    }

    @Test
    void requestsAreRateLimitedPerAccountEvenForUnknownEmails() throws Exception {
        String email = "limite-" + System.nanoTime() + "@example.com";
        for (int i = 0; i < 3; i++) {
            forgot(email).andExpect(status().isAccepted());
        }
        forgot(email)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void theOldMailEndpointsAreGone() throws Exception {
        // Antes eran públicos: /mail/sendMail decía si el correo existía y el token viajaba en la ruta.
        mockMvc.perform(post("/mail/sendMail").contentType(MediaType.TEXT_PLAIN).content("juan.perez@healthybites.com"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions forgot(String email) throws Exception {
        return mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo": "%s"}
                        """.formatted(email)));
    }

    private ResultActions reset(String token, String password) throws Exception {
        return mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token": "%s", "contrasena": "%s"}
                        """.formatted(token, password)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, password)));
    }

    private PasswordResetRequested singleEvent() {
        List<PasswordResetRequested> sent = events.stream(PasswordResetRequested.class).toList();
        assertThat(sent).hasSize(1);
        return sent.getFirst();
    }

    private static String tokenOf(PasswordResetRequested event) {
        return event.link().substring(event.link().indexOf("#token=") + "#token=".length());
    }

    private List<String> storedHashes(Registered user) {
        return jdbc.queryForList("select token_hash from password_reset_tokens where usuario_id = ?",
                String.class, user.usuarioId());
    }

    private String refreshCookieOf(MockHttpServletResponse response) {
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull().startsWith(cookieName + "=");
        return setCookie.substring(cookieName.length() + 1, setCookie.indexOf(';'));
    }
}
