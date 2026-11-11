package com.healthybites.service.impl;

import com.healthybites.dto.ContactMessageDTO;
import com.healthybites.exception.TooManyRequestsException;
import com.healthybites.integration.notification.email.dto.Mail;
import com.healthybites.integration.notification.email.service.EmailService;
import com.healthybites.security.ContactRateLimiter;
import com.healthybites.service.ContactService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Service
public class ContactServiceImpl implements ContactService {

    static final String TEMPLATE = "email/contact-message-template";

    private final EmailService emailService;
    private final ContactRateLimiter rateLimiter;
    private final String mailFrom;
    private final String recipient;

    public ContactServiceImpl(EmailService emailService,
                              ContactRateLimiter rateLimiter,
                              @Value("${spring.mail.username}") String mailFrom,
                              @Value("${healthybites.contact.recipient:${spring.mail.username}}") String recipient) {
        this.emailService = emailService;
        this.rateLimiter = rateLimiter;
        this.mailFrom = mailFrom;
        this.recipient = recipient;
    }

    @Override
    public void sendMessage(ContactMessageDTO message, String clientKey) {
        // Se responde igual que un envío correcto para no darle pistas al bot.
        if (StringUtils.hasText(message.getWebsite())) {
            log.info("Mensaje de contacto descartado: honeypot con valor");
            return;
        }

        if (!rateLimiter.tryAcquire(clientKey)) {
            throw new TooManyRequestsException(
                    "Recibimos demasiados mensajes. Inténtalo de nuevo en unos minutos.");
        }

        String nombre = singleLine(message.getNombre());
        String apellido = singleLine(message.getApellido());
        String correo = singleLine(message.getCorreo());

        Map<String, Object> model = Map.of(
                "nombre", nombre,
                "apellido", apellido,
                "correo", correo,
                "mensaje", message.getMensaje().trim());

        Mail mail = emailService.createMail(
                recipient,
                "Nuevo mensaje de contacto: " + nombre + " " + apellido,
                model,
                mailFrom);
        // El equipo responde directamente al visitante. Nunca se le envía un correo automático:
        // eso convertiría el endpoint en un relay de spam hacia direcciones arbitrarias.
        mail.setReplyTo(correo);

        try {
            emailService.sendEmail(mail, TEMPLATE);
        } catch (MessagingException e) {
            log.error("No se pudo preparar el correo de contacto", e);
            throw new MailPreparationException("No se pudo preparar el correo de contacto", e);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo de contacto", e);
            throw e;
        }
    }

    /** Quita saltos de línea y caracteres de control (evita inyección en las cabeceras del correo). */
    private static String singleLine(String value) {
        return value.replaceAll("\\p{Cntrl}+", " ").trim();
    }
}
