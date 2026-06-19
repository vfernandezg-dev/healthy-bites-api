package com.healthybites.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String correo;
}

