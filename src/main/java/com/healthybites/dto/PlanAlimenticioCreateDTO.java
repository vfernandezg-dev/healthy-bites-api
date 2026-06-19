package com.healthybites.dto;

import com.healthybites.model.enums.PlanObjetivo;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PlanAlimenticioCreateDTO {

    @NotNull(message = "El plan objetivo es obligatorio")
    private PlanObjetivo planObjetivo;

    @NotBlank(message = "La descripcion es obligatorio")
    private String descripcion;

    @Positive(message = "La duracion en dias debe ser un numero positivo")
    @NotNull(message = "La duracion en dias es obligatoria")
    private int duracionDias;

    private boolean esGratis;

    private Integer nutricionistaId;
}
