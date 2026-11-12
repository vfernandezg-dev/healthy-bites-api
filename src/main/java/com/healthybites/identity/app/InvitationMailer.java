package com.healthybites.identity.app;

import com.healthybites.shared.mail.MailService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Correo de invitación, cuando la invitación ya está guardada y en otro hilo. */
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
        Map<String, Object> model = Map.of(
                "name", event.firstName(),
                "acceptUrl", event.link(),
                "days", event.validFor().toDays());
        try {
            mailService.send(
                    event.email(),
                    "Te invitamos a Healthy Bites como nutricionista",
                    "email/nutritionist-invitation-template",
                    model);
        } catch (MailException e) {
            log.warn("No se pudo enviar la invitación: {}", e.getMessage());
        }
    }
}
