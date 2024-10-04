package com.healthybites.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class RachaDTO {

    private Integer id;

    @NotNull(message = "El número de días consecutivos es obligatorio")
    private Integer diasConsecutivos; // Días consecutivos

    @NotNull(message = "La fecha del último registro es obligatoria")
    private Date ultimaFechaRegistro; // Última fecha de registro
}
