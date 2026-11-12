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
 * Correo de invitación, cuando la invitación ya está guardada y en otro hilo. Como el del restablecimiento, no es
 * un evento persistido: el enlace lleva el token en claro. Si falla, el ADMIN puede reenviar la invitación.
 */
@Slf4j
@Component
class InvitationMailer {

    private final MailService mailService;

    InvitationMailer(MailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(InvitationIssued event) {
        EmailContent email = EmailContent.builder("Te invitamos a Healthy Bites como nutricionista")
                .title("Únete a Healthy Bites como nutricionista")
                .greeting(event.firstName())
                .paragraph("El equipo de Healthy Bites te invita a unirte como nutricionista. Desde tu cuenta podrás"
                        + " crear planes alimenticios, publicar contenido y acompañar a tus clientes.")
                .paragraph("Crea tu contraseña con el botón. El enlace caduca en "
                        + event.validFor().toDays() + " días y solo se puede usar una vez.")
                .action("Crear mi cuenta", event.link())
                .footnote("Si no esperabas esta invitación, puedes ignorar este correo.")
                .build();
        try {
            mailService.send(event.email(), email);
        } catch (MailException e) {
            log.warn("No se pudo enviar la invitación: {}", e.getMessage());
        }
    }
}
