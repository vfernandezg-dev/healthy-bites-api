package com.healthybites.mapper;

import com.healthybites.dto.RecompensaDTO;
import com.healthybites.model.entity.Recompensa;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RecompensaMapper {

    private final ModelMapper modelMapper;

    public RecompensaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public RecompensaDTO toDto(Recompensa recompensa) {
        return modelMapper.map(recompensa, RecompensaDTO.class);
    }

    public Recompensa toEntity(RecompensaDTO recompensaDTO) {
        return modelMapper.map(recompensaDTO, Recompensa.class);
    }
}
