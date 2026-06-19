package com.healthybites.dto;

import com.healthybites.model.entity.Meta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MetaDTO {
    private Integer id;

    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 100, message = "El nombre de la meta debe tener 100 caracteres o menos")
    private String nombre;

    @NotBlank(message = "La descripcion de la meta es obligatoria")
    @Size(max = 500, message = "La descripcion de la meta debe tener 500 caracteres o menos")
    private String descripcion;

    private double pesoObjetivo;
    private EstadoMeta estado;
    private ClienteDTO cliente;

    public MetaDTO() {

    }
    // Constructor que acepta un objeto Meta
    public MetaDTO(Meta meta) {
        this.id = meta.getId();
        this.nombre = meta.getNombre();
        this.descripcion = meta.getDescripcion();
        this.pesoObjetivo = meta.getPesoObjetivo();
        /*
        // Si la relación con Cliente es opcional, verifica que no sea null
        if (meta.getCliente() != null) {
            this.cliente = new ClienteDTO(meta.getCliente());
        }*/
    }
}
