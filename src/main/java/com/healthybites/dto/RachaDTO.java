package com.healthybites.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RachaDTO {

    private Integer clienteId;
    private String clienteNombre;

    private Integer recompensaId;
    private String recompensaNombre;

    @PositiveOrZero(message = "El contador de días no puede ser negativo")
    private Integer contadorDias; // Días consecutivos acumulados

    private boolean entregada;

    private LocalDateTime ultimaFechaRegistro; // Última fecha de registro
}
