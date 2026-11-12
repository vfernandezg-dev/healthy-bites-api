package com.healthybites.contact.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Mensaje del formulario de contacto de la landing. Los nombres JSON son el contrato con la landing (en español);
 * las reglas coinciden con su validación (landing-page/src/lib/contact.ts).
 *
 * @param website honeypot: campo oculto en el formulario; si llega con valor, el envío lo hizo un bot
 */
public record ContactMessage(
        @JsonProperty("nombre")
        @NotBlank(message = "El nombre es obligatorio") @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres") String firstName,

        @JsonProperty("apellido")
        @NotBlank(message = "El apellido es obligatorio") @Size(min = 2, max = 60, message = "El apellido debe tener entre 2 y 60 caracteres") String lastName,

        @JsonProperty("correo")
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un email válido") @Size(max = 254, message = "El correo no puede superar los 254 caracteres") String email,

        @JsonProperty("mensaje")
        @NotBlank(message = "El mensaje es obligatorio") @Size(min = 10, max = 2000, message = "El mensaje debe tener entre 10 y 2000 caracteres") String message,

        @JsonProperty("website") @Nullable String website) {}
