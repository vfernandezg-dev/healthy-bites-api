package com.healthybites.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GrupoCreateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre debe tener 50 caracteres o menos")
    private String nombre;

    @NotNull(message = "Indicar si el grupo es privado o no")
    private boolean esPrivado;

    private Integer clienteId;
}
