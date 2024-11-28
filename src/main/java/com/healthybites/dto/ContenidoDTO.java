package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import lombok.Data;

@Data
public class ContenidoDTO {
    private Integer id;
    private String titulo;
    private String descripcion;
    private String slug;
    private TipoContenido tipoContenido;
    private CategoriaContenido categoriaContenido;
    private String coverPath;
    private String filePath;
    private boolean esGratis;
    private String nutricionistaNombre;
}
