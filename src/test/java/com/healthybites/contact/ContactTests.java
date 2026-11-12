package com.healthybites.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Contrato con la landing ({@code POST /contact}): mismos campos en español y mismos códigos (204, 422, 429, 503).
 */
class ContactTests extends IntegrationTest {

    private static final AtomicInteger NEXT_IP = new AtomicInteger(1);

    private static final String VALID = """
            {"nombre": "Ana", "apellido": "Torres", "correo": "ana@correo.com",
             "mensaje": "Quisiera más información sobre los planes.", "website": ""}
            """;

    @Test
    void aValidMessageReachesTheTeamWithTheVisitorAsReplyTo() throws Exception {
        send(VALID, nextIp()).andExpect(status().isNoContent());

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(sent.capture());
        assertThat(sent.getValue().getReplyTo()).extracting(Address::toString).containsExactly("ana@correo.com");
        assertThat(sent.getValue().getRecipients(Message.RecipientType.TO))
                .extracting(Address::toString)
                .containsExactly("test@healthybites.local");
        assertThat(sent.getValue().getSubject()).isEqualTo("Nuevo mensaje de contacto: Ana Torres");
    }

    @Test
    void invalidFieldsAre422WithTheirMessages() throws Exception {
        send("""
                        {"nombre": "A", "apellido": "Torres", "correo": "no-es-un-correo", "mensaje": "corto"}
                        """, nextIp())
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("validation-failed"))
                .andExpect(jsonPath("$.errors.length()").value(3));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void theHoneypotIsDiscardedSilently() throws Exception {
        send(VALID.replace("\"website\": \"\"", "\"website\": \"https://spam.example\""), nextIp())
                .andExpect(status().isNoContent());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void eachVisitorIsRateLimitedWithRetryAfter() throws Exception {
        String ip = nextIp();
        for (int i = 0; i < 5; i++) {
            send(VALID, ip).andExpect(status().isNoContent());
        }
        send(VALID, ip).andExpect(status().isTooManyRequests()).andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void aMailServerFailureIs503SoTheLandingOffersMailto() throws Exception {
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));

        send(VALID, nextIp())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("mail-unavailable"));
    }

    private ResultActions send(String body, String ip) throws Exception {
        return mockMvc.perform(post("/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }

    private static String nextIp() {
        return "203.0.113." + NEXT_IP.getAndIncrement();
    }
}
