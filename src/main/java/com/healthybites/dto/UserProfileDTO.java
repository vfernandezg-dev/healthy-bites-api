package com.healthybites.dto;

import com.healthybites.model.enums.ERol;
import lombok.Data;

@Data
public class UserProfileDTO {
    private Integer id;
    private String correo;
    private ERol rol;

    private String nombre;
    private String apellido;

    private String sexo;
    private int edad;
    private double altura;
    private double peso;

    private String bio;
}
