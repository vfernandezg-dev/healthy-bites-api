package com.healthybites.dto;

import com.healthybites.model.enums.PlanObjetivo;
import lombok.Data;

import java.util.List;

@Data
public class PlanAlimenticioDTO {
    private Integer id;
    private PlanObjetivo planObjetivo;
    private String descripcion;
    private int duracionDias;
    private boolean esGratis;
    private String nutricionistaNombre;
    private int CaloriasTotales;
}
