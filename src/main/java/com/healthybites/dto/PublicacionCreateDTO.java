package com.healthybites.dto;

import lombok.Data;

@Data
public class PublicacionCreateDTO {
    private String titulo;
    private String descripcion;
    private Integer clienteId;
    private Integer grupoId;
}
