package com.healthybites.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Mensaje del formulario de contacto de la landing page.
 * Las reglas coinciden con la validación del cliente (healthy-bites-landing/src/lib/contact.ts).
 */
@Data
public class ContactMessageDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 60, message = "El apellido debe tener entre 2 y 60 caracteres")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 254, message = "El correo no puede superar los 254 caracteres")
    private String correo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 2000, message = "El mensaje debe tener entre 10 y 2000 caracteres")
    private String mensaje;

    /** Honeypot: campo oculto en el formulario. Si llega con valor, el envío lo hizo un bot. */
    private String website;
}
