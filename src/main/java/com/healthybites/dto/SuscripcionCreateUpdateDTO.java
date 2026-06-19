package com.healthybites.dto;

import com.healthybites.model.enums.TipoSuscripcion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuscripcionCreateUpdateDTO {
    private Integer id;

    @NotNull(message= "Debes escoger un tipo de suscripción.")
    private TipoSuscripcion tipoSuscripcion;

    @NotNull(message = "Debe ingresar el id del cliente.")
    private Integer clienteId;
}
