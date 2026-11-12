package com.healthybites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotBlank(message = "El enlace no es válido")
    @Size(max = 128, message = "El enlace no es válido")
    private String token;

    // Misma política que el registro: bcrypt solo usa los primeros 72 bytes.
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 10, max = 64, message = "La contraseña debe tener entre 10 y 64 caracteres")
    private String contrasena;
}
