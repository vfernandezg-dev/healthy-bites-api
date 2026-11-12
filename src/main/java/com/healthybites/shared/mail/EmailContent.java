package com.healthybites.shared.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Correo con el diseño común de Healthy Bites ({@code templates/email/notification.html}): título, saludo, párrafos,
 * una tabla opcional de datos, un botón y una nota final. Todo el texto se escapa al pintarlo y el correo lleva
 * también una versión en texto plano.
 *
 * <pre>{@code
 * EmailContent.builder("Restablece tu contraseña")
 *         .greeting(firstName)
 *         .paragraph("Pulsa el botón para elegir una contraseña nueva.")
 *         .action("Elegir contraseña", link)
 *         .build();
 * }</pre>
 */
public record EmailContent(
        String subject,
        String title,
        @Nullable String greetingName,
        List<String> paragraphs,
        List<Detail> details,
        @Nullable Action action,
        @Nullable String footnote) {

    /** Una fila de la tabla de datos (por ejemplo, «Importe · 5,99 USD»). */
    public record Detail(String label, String value) {}

    /** El botón principal del correo. */
    public record Action(String label, String url) {}

    public EmailContent {
        paragraphs = List.copyOf(paragraphs);
        details = List.copyOf(details);
    }

    public static Builder builder(String subject) {
        return new Builder(subject);
    }

    /** Variables de la plantilla HTML (las opcionales que faltan van como null). */
    Map<String, Object> model() {
        Map<String, Object> model = new HashMap<>();
        model.put("title", title);
        model.put("greetingName", greetingName);
        model.put("preheader", paragraphs.isEmpty() ? title : paragraphs.getFirst());
        model.put("paragraphs", paragraphs);
        model.put("details", details);
        model.put("action", action);
        model.put("footnote", footnote);
        return model;
    }

    /** La versión en texto plano (clientes sin HTML y filtros de spam que la comparan con la HTML). */
    String plainText() {
        StringBuilder text = new StringBuilder(title).append("\n\n");
        if (greetingName != null) {
            text.append("Hola, ").append(greetingName).append(":\n\n");
        }
        paragraphs.forEach(paragraph -> text.append(paragraph).append("\n\n"));
        details.forEach(detail ->
                text.append(detail.label()).append(": ").append(detail.value()).append('\n'));
        if (!details.isEmpty()) {
            text.append('\n');
        }
        if (action != null) {
            text.append(action.label()).append(": ").append(action.url()).append("\n\n");
        }
        if (footnote != null) {
            text.append(footnote).append("\n\n");
        }
        return text.append("— El equipo de Healthy Bites\n").toString();
    }

    public static final class Builder {

        private final String subject;
        private @Nullable String title;
        private @Nullable String greetingName;
        private final List<String> paragraphs = new ArrayList<>();
        private final List<Detail> details = new ArrayList<>();
        private @Nullable Action action;
        private @Nullable String footnote;

        private Builder(String subject) {
            this.subject = Objects.requireNonNull(subject);
        }

        /** Título visible del correo (por defecto, el asunto). */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /** Nombre para el saludo «Hola, {nombre}:». */
        public Builder greeting(String name) {
            this.greetingName = name;
            return this;
        }

        public Builder paragraph(String paragraph) {
            paragraphs.add(paragraph);
            return this;
        }

        public Builder detail(String label, String value) {
            details.add(new Detail(label, value));
            return this;
        }

        public Builder action(String label, String url) {
            this.action = new Action(label, url);
            return this;
        }

        /** Texto pequeño al final (por qué recibe el correo, qué hacer si no lo esperaba...). */
        public Builder footnote(String footnote) {
            this.footnote = footnote;
            return this;
        }

        public EmailContent build() {
            return new EmailContent(
                    subject, title != null ? title : subject, greetingName, paragraphs, details, action, footnote);
        }
    }
}
