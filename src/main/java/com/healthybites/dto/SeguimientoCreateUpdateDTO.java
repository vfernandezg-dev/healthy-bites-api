package com.healthybites.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeguimientoCreateUpdateDTO {
    private Integer id;

    private String fecha;

    private double pesoDelDia;

    private String observaciones;


}
