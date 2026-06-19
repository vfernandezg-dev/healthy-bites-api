package com.healthybites.dto;

import lombok.Data;

@Data
public class GrupoDTO {
    private Integer id;
    private String nombre;
    private int cantidadMiembros;
    private boolean esPrivado;
    private String clienteNombre;
}
