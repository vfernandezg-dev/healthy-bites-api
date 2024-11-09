package com.healthybites.dto;

import lombok.Data;

@Data
public class RecompensaCreateUpdateDTO {
    private String nombre;
    private String descripcion;
    private int diasRequeridos;
    private Integer nutricionistaId;

    private Integer contenidoId;
    private Integer planAlimenticioId;
}
