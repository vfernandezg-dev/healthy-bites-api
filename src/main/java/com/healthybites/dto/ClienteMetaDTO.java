package com.healthybites.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteMetaDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Email(message = "El correo debe ser válido")
    private String correo;

    @NotBlank(message = "El sexo es obligatorio")
    private String sexo;

    private int edad;
    private double altura;
    private double peso;

    private Integer metaID;
    private String metaNombre;
    private String metaDescripcion;
    private double pesoObjetivo;
}
