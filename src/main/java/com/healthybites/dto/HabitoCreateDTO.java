package com.healthybites.dto;

import lombok.Data;

@Data
public class HabitoCreateDTO {
    private String nombre;
    private String fechaRegistro;
    private float hidratacion;
    private float alimentacion;
    private float ejercicio;
    private float calidadDeSueno;
    private Integer clienteId;
}
