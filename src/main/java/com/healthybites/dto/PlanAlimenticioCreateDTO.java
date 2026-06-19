package com.healthybites.dto;

import com.healthybites.model.enums.PlanObjetivo;
import lombok.Data;

import java.util.List;

@Data
public class PlanAlimenticioCreateDTO {
    private PlanObjetivo planObjetivo;
    private String descripcion;
    private int duracionDias;
    private boolean esGratis;
    private Integer nutricionistaId;
    private List<Integer> comidasDiariasIds;
}
