package com.healthybites.dto;

import lombok.Data;

@Data
public class PublicacionDTO {
    private Integer id;
    private String titulo;
    private String descripcion;
    private String nombreCliente;
    private String nombreGrupo;
}
