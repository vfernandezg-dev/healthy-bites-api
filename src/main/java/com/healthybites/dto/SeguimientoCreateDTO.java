package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SeguimientoCreateDTO {
    private String fecha;

    @Positive(message = "El campo peso del día debe ser un número positivo")
    @NotNull(message = "El campo peso del día es obligatorio")
    private double pesoDelDia;

    @NotBlank(message = "El campo observaciones es obligatorio")
    @Size(max = 100, message = "Las observaciones deben tener 100 caracteres o menos")
    private String observaciones;

}
