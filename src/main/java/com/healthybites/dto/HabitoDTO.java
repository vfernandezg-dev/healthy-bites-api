package com.healthybites.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HabitoDTO {
    private Integer id;
    private String nombre;
    private LocalDateTime fechaRegistro;
    private float hidratacion;
    private float alimentacion;
    private float ejercicio;
    private float calidadDeSueno;
    private String nombreCliente;
}
