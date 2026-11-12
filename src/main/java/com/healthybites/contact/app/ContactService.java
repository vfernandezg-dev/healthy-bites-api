package com.healthybites.contact.app;

import com.healthybites.shared.mail.MailService;
import com.healthybites.shared.web.ApiException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Reenvía al equipo los mensajes de la landing. Es síncrono a propósito: si el correo falla, la landing recibe un
 * 503 y ofrece {@code mailto:} al visitante.
 */
@Slf4j
@Service
public class ContactService {

    static final String TEMPLATE = "email/contact-message-template";

    private final MailService mailService;
    private final ContactRateLimiter rateLimiter;
    private final String recipient;

    public ContactService(MailService mailService, ContactRateLimiter rateLimiter, ContactProperties properties) {
        this.mailService = mailService;
        this.rateLimiter = rateLimiter;
        this.recipient = properties.recipient();
    }

    /** Mensaje ya validado; {@code honeypot} es el campo oculto que solo rellenan los bots. */
    public record Message(
            String firstName,
            String lastName,
            String email,
            String message,
            @Nullable String honeypot) {}

    public void send(Message message, String clientKey) {
        // Se responde igual que un envío correcto para no darle pistas al bot.
        if (StringUtils.hasText(message.honeypot())) {
            log.info("Mensaje de contacto descartado: honeypot con valor");
            return;
        }
        rateLimiter.acquire(clientKey).ifPresent(wait -> {
            throw ApiException.tooManyRequests(
                    "Recibimos demasiados mensajes. Inténtalo de nuevo en unos minutos.", wait);
        });
        String firstName = singleLine(message.firstName());
        String lastName = singleLine(message.lastName());
        String email = singleLine(message.email());
        Map<String, Object> model = Map.of(
                "nombre", firstName,
                "apellido", lastName,
                "correo", email,
                "mensaje", message.message().trim());
        // El equipo responde directamente al visitante. Nunca se le envía un correo automático: eso convertiría el
        // endpoint en un relay de spam hacia direcciones arbitrarias.
        // Si el SMTP falla, GlobalExceptionHandler lo registra y responde 503 mail-unavailable.
        mailService.send(recipient, "Nuevo mensaje de contacto: " + firstName + " " + lastName, TEMPLATE, model, email);
    }

    /** Quita saltos de línea y caracteres de control (evita inyección en las cabeceras del correo). */
    private static String singleLine(String value) {
        return value.replaceAll("\\p{Cntrl}+", " ").trim();
    }
}
