package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class MetaCreateDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 100, message = "La descripción debe tener 100 caracteres o menos")
    private String descripcion;

    @Positive(message = "El peso debe ser un numero positivo")
    @NotNull(message = "Indique el peso objetivo")
    private double pesoObjetivo;

    private Integer clienteId;
}
