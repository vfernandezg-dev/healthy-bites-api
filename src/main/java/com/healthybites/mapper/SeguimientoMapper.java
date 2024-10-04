package com.healthybites.mapper;

import com.healthybites.dto.SeguimientoDTO;
import com.healthybites.model.entity.Seguimiento;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class SeguimientoMapper {
    private final ModelMapper modelMapper;

    public SeguimientoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public SeguimientoDTO toSeguimientoDTO(Seguimiento seguimiento) {
        return modelMapper.map(seguimiento, SeguimientoDTO.class);
    }

    public Seguimiento toEntity(SeguimientoDTO seguimientoDTO) {
        return modelMapper.map(seguimientoDTO, Seguimiento.class);
    }
}
