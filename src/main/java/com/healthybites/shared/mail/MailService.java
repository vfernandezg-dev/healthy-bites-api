package com.healthybites.shared.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Envío de correos HTML con plantillas Thymeleaf ({@code templates/email/*.html}). Todos salen desde la cuenta
 * configurada ({@code spring.mail.username}).
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String from;

    public MailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    /**
     * @param template plantilla sin extensión, por ejemplo {@code email/password-reset}
     * @param replyTo  dirección a la que responder (el formulario de contacto usa la del visitante)
     * @throws MailException si el servidor de correo falla
     */
    public void send(String to, String subject, String template, Map<String, Object> model, @Nullable String replyTo) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            Context context = new Context();
            context.setVariables(model);
            helper.setText(templateEngine.process(template, context), true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(from);
            if (replyTo != null) {
                helper.setReplyTo(replyTo);
            }
        } catch (MessagingException e) {
            throw new MailPreparationException("No se pudo preparar el correo", e);
        }
        mailSender.send(message);
    }

    public void send(String to, String subject, String template, Map<String, Object> model) {
        send(to, subject, template, model, null);
    }
}
