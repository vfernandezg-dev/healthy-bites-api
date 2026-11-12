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
 *
 * <p>Los correos para las personas usan {@link EmailContent} y el diseño común; la plantilla libre queda para casos
 * especiales, como el aviso interno del formulario de contacto.
 */
@Service
public class MailService {

    static final String BRANDED_TEMPLATE = "email/notification";

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
     * Correo con el diseño común (HTML y texto plano).
     *
     * @throws MailException si el servidor de correo falla
     */
    public void send(String to, EmailContent content) {
        String html = render(BRANDED_TEMPLATE, content.model());
        deliver(to, content.subject(), content.plainText(), html, null);
    }

    /**
     * @param template plantilla sin extensión, por ejemplo {@code email/contact-message-template}
     * @param replyTo  dirección a la que responder (el formulario de contacto usa la del visitante)
     * @throws MailException si el servidor de correo falla
     */
    public void send(String to, String subject, String template, Map<String, Object> model, @Nullable String replyTo) {
        deliver(to, subject, null, render(template, model), replyTo);
    }

    public void send(String to, String subject, String template, Map<String, Object> model) {
        send(to, subject, template, model, null);
    }

    private String render(String template, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process(template, context);
    }

    private void deliver(String to, String subject, @Nullable String plainText, String html, @Nullable String replyTo) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            if (plainText != null) {
                helper.setText(plainText, html);
            } else {
                helper.setText(html, true);
            }
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
}
