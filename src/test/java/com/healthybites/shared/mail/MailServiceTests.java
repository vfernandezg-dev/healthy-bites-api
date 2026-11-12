package com.healthybites.shared.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

class MailServiceTests {

    @Test
    void elRemitenteEsElConfiguradoConSuNombreYNoLaCuentaSmtp() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        SpringTemplateEngine templates = mock(SpringTemplateEngine.class);
        when(templates.process(anyString(), any(IContext.class))).thenReturn("<p>Hola</p>");
        MailService mail = new MailService(sender, templates, "Healthy Bites <projects@vfernandezg.dev>");

        mail.send("maria@correo.com", "Asunto", "email/plantilla", Map.of());

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(sent.capture());
        InternetAddress from = (InternetAddress) sent.getValue().getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("projects@vfernandezg.dev");
        assertThat(from.getPersonal()).isEqualTo("Healthy Bites");
    }
}
