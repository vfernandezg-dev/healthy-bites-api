package com.healthybites.service.checkout;

import com.healthybites.config.properties.AppProperties;
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
 * Correo de confirmación de compra. Va después del commit y en otro hilo: antes se enviaba dentro de la captura y,
 * si el SMTP fallaba, el usuario veía un error aunque PayPal ya le hubiera cobrado.
 */
@Slf4j
@Component
public class SubscriptionPaidMailer {

    private final EmailService emailService;
    private final AppProperties appProperties;
    private final String mailFrom;

    public SubscriptionPaidMailer(EmailService emailService, AppProperties appProperties,
                                  @Value("${spring.mail.username}") String mailFrom) {
        this.emailService = emailService;
        this.appProperties = appProperties;
        this.mailFrom = mailFrom;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(SubscriptionPaid event) {
        Map<String, Object> model = Map.of(
                "user", event.email(),
                "total", event.total(),
                "orderUrl", appProperties.frontend("cliente/perfil"));
        Mail mail = emailService.createMail(event.email(), "Confirmación de compra", model, mailFrom);
        try {
            emailService.sendEmail(mail, "email/subscription-confirmation-template");
        } catch (MessagingException | MailException e) {
            log.warn("No se pudo enviar la confirmación de compra: {}", e.getMessage());
        }
    }
}
