package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteDTO {
    private Integer id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no es obligatorio")
    @Size(max = 50, message = "El apellido no puede tener más de 50 caracteres")
    private String apellido;

    @Size(max = 10, message = "Masculino o femenino")
    private String sexo;

    @Email(message = "El correo debe ser un email válido")
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @Positive(message = "La edad debe ser un número positivo")
    private int edad;

    @Positive(message = "La altura debe ser un número positivo")
    private double altura;

    @Positive(message = "El peso debe ser un número positivo")
    private double peso;


}
