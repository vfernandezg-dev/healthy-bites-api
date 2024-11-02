package com.healthybites.dto;

import lombok.Data;

@Data
public class ComentarioCreateDTO {
    private String mensaje;
    private Integer clienteId;
    private Integer publicacionId;
}
