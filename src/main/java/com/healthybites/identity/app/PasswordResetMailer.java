package com.healthybites.identity.app;

import com.healthybites.shared.mail.MailService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía el correo de restablecimiento cuando la transacción ya está confirmada (el enlace existe) y en otro hilo:
 * la respuesta de {@code /auth/forgot-password} tarda lo mismo exista o no la cuenta, y un fallo del SMTP no la
 * convierte en un error que delate que el correo está registrado.
 */
@Slf4j
@Component
class PasswordResetMailer {

    private final MailService mailService;

    PasswordResetMailer(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(PasswordResetRequested event) {
        Map<String, Object> model = Map.of(
                "user", event.firstName(),
                "resetUrl", event.link(),
                "minutes", event.validFor().toMinutes());
        try {
            mailService.send(
                    event.email(), "Restablece tu contraseña de Healthy Bites", "email/password-reset-template", model);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo de restablecimiento: {}", e.getMessage());
        }
    }
}
