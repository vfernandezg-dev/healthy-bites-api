package com.healthybites.service.impl;

import com.healthybites.dto.ContactMessageDTO;
import com.healthybites.exception.TooManyRequestsException;
import com.healthybites.integration.notification.email.dto.Mail;
import com.healthybites.integration.notification.email.service.EmailService;
import com.healthybites.security.ContactRateLimiter;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    private static final String FROM = "remitente@healthybites.test";
    private static final String RECIPIENT = "equipo@healthybites.test";

    @Mock
    private EmailService emailService;

    @Mock
    private ContactRateLimiter rateLimiter;

    private ContactServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContactServiceImpl(emailService, rateLimiter, FROM, RECIPIENT);
    }

    private static ContactMessageDTO message() {
        ContactMessageDTO dto = new ContactMessageDTO();
        dto.setNombre("  Ana ");
        dto.setApellido("Torres");
        dto.setCorreo("ana@correo.com");
        dto.setMensaje("  Quisiera más información sobre los planes.  ");
        return dto;
    }

    private void stubCreateMail() {
        when(emailService.createMail(anyString(), anyString(), anyMap(), anyString()))
                .thenAnswer(invocation -> new Mail(
                        invocation.getArgument(3),
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        null));
    }

    @Test
    void enviaElMensajeAlEquipoConReplyToDelVisitante() throws Exception {
        when(rateLimiter.tryAcquire("1.1.1.1")).thenReturn(true);
        stubCreateMail();

        service.sendMessage(message(), "1.1.1.1");

        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(emailService).sendEmail(mailCaptor.capture(), eq(ContactServiceImpl.TEMPLATE));
        Mail mail = mailCaptor.getValue();
        assertThat(mail.getTo()).isEqualTo(RECIPIENT);
        assertThat(mail.getFrom()).isEqualTo(FROM);
        assertThat(mail.getReplyTo()).isEqualTo("ana@correo.com");
        assertThat(mail.getSubject()).isEqualTo("Nuevo mensaje de contacto: Ana Torres");
        assertThat(mail.getModel()).isEqualTo(Map.of(
                "nombre", "Ana",
                "apellido", "Torres",
                "correo", "ana@correo.com",
                "mensaje", "Quisiera más información sobre los planes."));
    }

    @Test
    void eliminaSaltosDeLineaDelAsunto() throws Exception {
        when(rateLimiter.tryAcquire(any())).thenReturn(true);
        stubCreateMail();
        ContactMessageDTO dto = message();
        dto.setNombre("Ana\r\nBcc: victima@correo.com");

        service.sendMessage(dto, "1.1.1.1");

        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(emailService).sendEmail(mailCaptor.capture(), anyString());
        assertThat(mailCaptor.getValue().getSubject()).doesNotContain("\r", "\n");
    }

    @Test
    void descartaEnSilencioCuandoElHoneypotTieneValor() {
        ContactMessageDTO dto = message();
        dto.setWebsite("https://spam.example");

        service.sendMessage(dto, "1.1.1.1");

        verifyNoInteractions(emailService, rateLimiter);
    }

    @Test
    void rechazaCuandoSeSuperaElLimite() throws Exception {
        when(rateLimiter.tryAcquire("1.1.1.1")).thenReturn(false);

        assertThatThrownBy(() -> service.sendMessage(message(), "1.1.1.1"))
                .isInstanceOf(TooManyRequestsException.class);
        verify(emailService, never()).sendEmail(any(), anyString());
    }

    @Test
    void propagaLosFallosDelServidorDeCorreoComoMailException() throws Exception {
        when(rateLimiter.tryAcquire(any())).thenReturn(true);
        stubCreateMail();
        doThrow(new MailSendException("SMTP caído")).when(emailService).sendEmail(any(), anyString());

        assertThatThrownBy(() -> service.sendMessage(message(), "1.1.1.1"))
                .isInstanceOf(MailSendException.class);
    }

    @Test
    void convierteErroresDePreparacionEnMailPreparationException() throws Exception {
        when(rateLimiter.tryAcquire(any())).thenReturn(true);
        stubCreateMail();
        doThrow(new MessagingException("plantilla inválida")).when(emailService).sendEmail(any(), anyString());

        assertThatThrownBy(() -> service.sendMessage(message(), "1.1.1.1"))
                .isInstanceOf(MailPreparationException.class);
    }
}
