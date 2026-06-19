package com.healthybites.dto;

import com.healthybites.model.enums.TipoSuscripcion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuscripcionCreateUpdateDTO {
    private Integer usuarioId;

    @NotNull(message= "Debes escoger un tipo de suscripción.")
    private TipoSuscripcion tipoSuscripcion;

}
