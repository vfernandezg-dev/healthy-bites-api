package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContenidoDetailsDTO {
    private Integer id;
    private String titulo;
    private String descripcion;
    private TipoContenido tipoContenido;
    private CategoriaContenido categoriaContenido;
    private boolean esGratis;
    private String nutricionistaNombre;
}
