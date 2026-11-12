package com.healthybites.identity.app;

import com.healthybites.shared.mail.EmailContent;
import com.healthybites.shared.mail.MailService;
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
 *
 * <p>No es un evento persistido a propósito: el enlace lleva el token en claro y no debe guardarse en el registro de
 * eventos. Si el SMTP falla, la persona puede volver a pedirlo.
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
        EmailContent email = EmailContent.builder("Restablece tu contraseña de Healthy Bites")
                .title("Restablece tu contraseña")
                .greeting(event.firstName())
                .paragraph("Hemos recibido una solicitud para restablecer la contraseña de tu cuenta. Pulsa el botón"
                        + " para elegir una nueva.")
                .paragraph("El enlace caduca en " + event.validFor().toMinutes()
                        + " minutos y solo se puede usar una vez. Al cambiarla, se cerrarán todas tus sesiones.")
                .action("Elegir una contraseña nueva", event.link())
                .footnote("Si no lo has pedido tú, ignora este correo: tu contraseña no cambia.")
                .build();
        try {
            mailService.send(event.email(), email);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo de restablecimiento: {}", e.getMessage());
        }
    }
}
