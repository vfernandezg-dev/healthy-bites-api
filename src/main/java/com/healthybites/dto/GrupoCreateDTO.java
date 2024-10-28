package com.healthybites.dto;

import lombok.Data;

@Data
public class GrupoCreateDTO {
    private String nombre;
    private boolean esPrivado;
    private Integer clienteId;
}
