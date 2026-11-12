package com.healthybites.security.password;

import com.healthybites.integration.notification.email.dto.Mail;
import com.healthybites.integration.notification.email.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Envía el correo de restablecimiento cuando la transacción ya está confirmada (el enlace existe) y en otro hilo:
 * la respuesta de {@code /auth/forgot-password} tarda lo mismo exista o no la cuenta, y un fallo del SMTP no la
 * convierte en un error que delate que el correo está registrado.
 */
@Slf4j
@Component
public class PasswordResetMailer {

    private final EmailService emailService;
    private final String mailFrom;

    public PasswordResetMailer(EmailService emailService, @Value("${spring.mail.username}") String mailFrom) {
        this.emailService = emailService;
        this.mailFrom = mailFrom;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(PasswordResetRequested event) {
        Map<String, Object> model = Map.of(
                "user", event.email(),
                "resetUrl", event.link(),
                "minutes", event.validFor().toMinutes());
        Mail mail = emailService.createMail(event.email(), "Restablece tu contraseña de Healthy Bites", model, mailFrom);
        try {
            emailService.sendEmail(mail, "email/password-reset-template");
        } catch (MessagingException | MailException e) {
            log.warn("No se pudo enviar el correo de restablecimiento: {}", e.getMessage());
        }
    }
}
