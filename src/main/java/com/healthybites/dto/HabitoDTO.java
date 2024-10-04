package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitoDTO {

    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotNull(message = "La hidratación no puede ser nula")
    @DecimalMin(value = "0.0", inclusive = false, message = "La hidratación debe ser mayor que 0")
    private Float hidratacion;

    @NotNull(message = "La alimentación no puede ser nula")
    @DecimalMin(value = "0.0", inclusive = false, message = "La alimentación debe ser mayor que 0")
    private Float alimentacion;

    @NotNull(message = "El ejercicio no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El ejercicio debe ser mayor que 0")
    private Float ejercicio;

    @NotNull(message = "La calidad de sueño no puede ser nula")
    @DecimalMin(value = "0.0", inclusive = false, message = "La calidad de sueño debe ser mayor que 0")
    private Float calidadDeSueno;
}
