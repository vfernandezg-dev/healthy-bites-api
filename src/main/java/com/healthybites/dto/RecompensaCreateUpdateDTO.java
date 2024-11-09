package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RecompensaCreateUpdateDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 100, message = "La descripción debe tener 100 caracteres o menos")
    private String descripcion;

    @Positive(message = "Los puntos requeridos deben ser un número positivo")
    @NotNull(message = "Los días requeridos son obligatorios")
    private int diasRequeridos;

    private Integer nutricionistaId;

    private Integer contenidoId;
    private Integer planAlimenticioId;
}
