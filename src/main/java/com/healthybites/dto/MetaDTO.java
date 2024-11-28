package com.healthybites.dto;

import lombok.Data;

import java.util.List;

@Data
public class MetaDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private double pesoObjetivo;
    private String clienteNombre;

    private Integer metaId;
}
