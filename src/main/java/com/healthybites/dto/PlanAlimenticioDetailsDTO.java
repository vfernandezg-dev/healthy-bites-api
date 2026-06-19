package com.healthybites.dto;

import com.healthybites.model.enums.PlanObjetivo;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlanAlimenticioDetailsDTO {
    private Integer id;
    private PlanObjetivo planObjetivo;
    private String descripcion;
    private int duracionDias;
    private boolean esGratis;
    private String nutricionistaNombre;
}
