package com.healthybites.mapper;

import com.healthybites.dto.HabitoDTO;
import com.healthybites.model.entity.Habito;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class HabitoMapper {

    private final ModelMapper modelMapper;

    public HabitoMapper(ModelMapper modelMapper) {
        this.modelMapper = new ModelMapper();
    }

    public HabitoDTO toDto(Habito habito) {
        return modelMapper.map(habito, HabitoDTO.class);
    }

    public Habito toEntity(HabitoDTO habitoDTO) {
        return modelMapper.map(habitoDTO, Habito.class);
    }
}
