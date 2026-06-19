package com.healthybites.dto;

import lombok.Data;

import java.util.List;

@Data
public class MetaCreateDTO {
    private String nombre;
    private String descripcion;
    private double pesoObjetivo;
    private Integer clienteId;
}
