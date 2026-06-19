package com.healthybites.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeguimientoDetailsDTO {
    private Integer id;

    private LocalDateTime fecha;

    private double pesoDelDia;

    private String observaciones;


}
