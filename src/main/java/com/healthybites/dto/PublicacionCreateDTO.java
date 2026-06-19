package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PublicacionCreateDTO {
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 100, message = "La descripción debe tener 100 caracteres o menos")
    private String descripcion;

    private Integer clienteId;
    private Integer grupoId;
}
