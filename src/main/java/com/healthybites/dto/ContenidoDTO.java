package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ContenidoDTO {
    private Integer id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede tener más de 100 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede tener más de 500 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de contenido es obligatorio")
    private TipoContenido tipoContenido; // Cambia de String a TipoContenido (Enum)

    @NotNull(message = "La categoría de contenido es obligatoria")
    private CategoriaContenido categoriaContenido;

    private boolean esGratis; // Indica si el contenido es gratuito
}
