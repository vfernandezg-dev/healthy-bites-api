package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaComida;
import lombok.Data;

@Data
public class ComidaDiariaDetailsDTO {
    private Integer id;
    private String nombreComida;
    private int calorias;
    private CategoriaComida categoria;
    private String nombrePlanAlimenticio;
}
