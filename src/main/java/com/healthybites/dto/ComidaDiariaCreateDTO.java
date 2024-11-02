package com.healthybites.dto;

import com.healthybites.model.enums.CategoriaComida;
import lombok.Data;

@Data
public class ComidaDiariaCreateDTO {
    private String nombreComida;

    private int calorias;

    private CategoriaComida categoria;

}
