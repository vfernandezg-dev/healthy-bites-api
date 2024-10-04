package com.healthybites.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RecompensaDTO {

    private Integer id;

    @NotBlank(message = "El nombre de la recompensa es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
    private String descripcion;

    @Min(value = 1, message = "Los días requeridos deben ser al menos 1")
    private int diasRequeridos;

    // Este campo puede ser opcional, dependiendo de si necesitas la relación en el DTO
    private Integer idCliente; // Para asociar la recompensa a un cliente
}
