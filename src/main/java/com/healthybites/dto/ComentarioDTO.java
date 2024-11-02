package com.healthybites.dto;

import lombok.Data;

@Data
public class ComentarioDTO {
    private Integer id;
    private String mensaje;
    private String nombreCliente;
    private String nombrePublicacion;
}
