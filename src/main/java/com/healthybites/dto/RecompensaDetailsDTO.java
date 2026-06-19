package com.healthybites.dto;

import com.healthybites.model.enums.PlanObjetivo;
import lombok.Data;

@Data
public class RecompensaDetailsDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private int diasRequeridos;
    private String nutricionistaNombre;

    private String contenidoTitulo;
    private PlanObjetivo planAlimenticioTitulo;
}
