package com.healthybites.dto;

import com.healthybites.model.entity.Cliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MetaDTO {
    private Integer id;
    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 100, message = "El nombre de la meta debe tener 100 caracteres o menos")
    private String nombre;
    @NotBlank(message = "La descripcion de la meta es obligatorio")
    @Size(max = 500, message = "La descripcion de la menta debe tener 500 caracteres o menos")
    private String descripcion;
    private double pesoObjetivo;
    private ClienteDTO cliente;
}
