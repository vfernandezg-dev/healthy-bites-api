package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContenidoCreateUpdateDTO {
    private Integer id;

    @NotBlank(message= "Debes ingresar un título.")
    @Size(max = 60, message = "El título no debe superar los 60 caracteres.")
    private String titulo;

    @NotBlank(message= "Debes ingresar una descripción.")
    @Size(max = 4000, message = "La descripción ha superado los 4000 caracteres.")
    private String descripcion;

    @NotNull(message= "Debes ingresar un tipo de contenido.")
    private TipoContenido tipoContenido;

    @NotNull(message= "Debes indicar que categoría de contenido estás ingresando.")
    private CategoriaContenido categoriaContenido;

    @NotNull(message= "Debes indicar si el contenido es gratis o no.")
    private boolean esGratis;

    @NotNull(message= "Debes ingresar el id del nutricionista.")
    private Integer nutricionistaId;
}