package com.healthybites.dto;

import lombok.Data;

@Data
public class ClienteMetaDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private String sexo;
    private int edad;
    private double altura;
    private double peso;

    private Integer metaID;
    private String metaNombre;
    private String metaDescripcion;
    private double pesoObjetivo;
}
