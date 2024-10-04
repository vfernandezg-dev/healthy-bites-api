package com.healthybites.mapper;

import com.healthybites.dto.RachaDTO;
import com.healthybites.model.entity.Racha;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RachaMapper {

    private final ModelMapper modelMapper;

    public RachaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public RachaDTO toDto(Racha racha) {
        return modelMapper.map(racha, RachaDTO.class);
    }

    public Racha toEntity(RachaDTO rachaDTO) {
        return modelMapper.map(rachaDTO, Racha.class);
    }
}
