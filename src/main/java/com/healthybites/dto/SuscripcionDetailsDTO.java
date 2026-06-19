package com.healthybites.dto;

import com.healthybites.model.enums.TipoSuscripcion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuscripcionDetailsDTO {
    private Integer id;
    private TipoSuscripcion tipoSuscripcion;
    private double precio;
}
