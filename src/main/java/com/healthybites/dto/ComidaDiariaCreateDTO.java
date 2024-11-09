package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaComida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComidaDiariaCreateDTO {

    @NotBlank(message = "El nombre de la comida es obligatorio")
    @Size(max = 50, message = "El nombre de la comida debe tener 50 caracteres o menos")
    private String nombreComida;

    @Positive(message = "Las calorias deben ser un numero positivo")
    @NotNull(message = "Las calorias son obligatorias")
    private int calorias;

    @NotNull(message = "La categoria de la comida es obligatoria")
    private CategoriaComida categoria;

}
