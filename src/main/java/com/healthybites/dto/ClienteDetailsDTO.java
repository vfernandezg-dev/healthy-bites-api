package com.healthybites.dto;

import lombok.Data;

@Data
public class ClienteDetailsDTO {

    private Integer id;
    private String nombre;
    private String apellido;
    private String sexo;
    private int edad;
    private double altura;
    private double peso;
}
