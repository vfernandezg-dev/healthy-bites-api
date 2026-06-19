package com.healthybites.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SeguimientoDTO {

    private Integer id; // ID del seguimiento

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha debe ser hoy o en el pasado")
    private LocalDate fecha; // Fecha del seguimiento

    @NotNull(message = "El peso del día es obligatorio")
    private Double pesoDelDia; // Peso registrado en el día

    private String observaciones; // Observaciones del seguimiento

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer clienteId; // ID del cliente relacionado
}
