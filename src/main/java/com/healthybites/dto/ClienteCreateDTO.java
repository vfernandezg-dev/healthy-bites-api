package com.healthybites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre debe tener 50 caracteres o menos")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido debe tener 50 caracteres o menos")
    private String apellido;

    @NotBlank(message = "El sexo es obligatorio")
    private String sexo;

    @NotNull(message = "La edad es obligatorio")
    private int edad;

    @NotNull(message = "La altura es obligatorio")
    private double altura;

    @NotNull(message = "El peso es obligatorio")
    private double peso;

}
