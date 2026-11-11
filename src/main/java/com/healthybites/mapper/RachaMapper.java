package com.healthybites.mapper;

import com.healthybites.dto.RachaDTO;
import com.healthybites.model.entity.Racha;
import org.springframework.stereotype.Component;

@Component
public class RachaMapper {

    // Las rachas se insertan con la query nativa RachaRepository.addRecompensaToCliente
    // (sus join columns son insertable=false), por eso solo hay mapeo entidad -> DTO.
    public RachaDTO toDto(Racha racha) {
        RachaDTO dto = new RachaDTO();
        if (racha.getCliente() != null) {
            dto.setClienteId(racha.getCliente().getId());
            dto.setClienteNombre(racha.getCliente().getNombre() + " " + racha.getCliente().getApellido());
        }
        if (racha.getRecompensa() != null) {
            dto.setRecompensaId(racha.getRecompensa().getId());
            dto.setRecompensaNombre(racha.getRecompensa().getNombre());
        }
        dto.setContadorDias(racha.getContadorDias());
        dto.setEntregada(racha.isEntregada());
        dto.setUltimaFechaRegistro(racha.getUltimaFechaRegistro());
        return dto;
    }
}
