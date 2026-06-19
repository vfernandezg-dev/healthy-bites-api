package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ComentarioCreateDTO {

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 255, message = "El mensaje debe tener 255 caracteres o menos")
    private String mensaje;

    private Integer clienteId;
}
