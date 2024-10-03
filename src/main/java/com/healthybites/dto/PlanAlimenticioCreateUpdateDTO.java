package com.healthybites.dto;

import com.healthybites.model.enums.PlanObjetivo;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlanAlimenticioCreateUpdateDTO {

    private Integer id;

    @NotNull(message = "El objetivo del plan solo puede ser: DEFICIT, MANTENIMIENTO_PESO, AUMENTO_MASA_MUSCULAR")
    private PlanObjetivo planObjetivo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
    private String descripcion;

    @Positive(message = "La duración del plan debe ser un número positivo")
    private int duracionDias;

    @AssertTrue(message = "El valor debe ser verdadero o falso")
    private boolean esGratis;

    @NotNull(message = "El ID del nutricionista es obligatorio")
    private Integer nutricionistaId;

}
