package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class HabitoCreateDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La fecha de registro es obligatoria y debe ser en formato DD-MM-YYYY")
    private String fechaRegistro;

    @NotNull(message = "La hidratación es obligatoria")
    @Min(value = 0, message = "La hidratación debe ser un valor positivo")
    private Float hidratacion;

    @NotNull(message = "La alimentación es obligatoria")
    @Min(value = 0, message = "La alimentación debe ser un valor positivo")
    private Float alimentacion;

    @NotNull(message = "El ejercicio es obligatorio")
    @Min(value = 0, message = "El ejercicio debe ser un valor positivo")
    private Float ejercicio;

    @NotNull(message = "La calidad de sueño es obligatoria")
    @Min(value = 0, message = "La calidad de sueño debe ser un valor positivo")
    private Float calidadDeSueno;

    private Integer clienteId;
}
