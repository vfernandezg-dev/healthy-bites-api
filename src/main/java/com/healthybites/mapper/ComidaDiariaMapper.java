package com.healthybites.mapper;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.model.entity.ComidaDiaria;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ComidaDiariaMapper {

    private final ModelMapper modelMapper;

    public ComidaDiariaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public ComidaDiariaDTO toDTO(ComidaDiaria comidaDiaria) {
        ComidaDiariaDTO comidaDiariaDTO = modelMapper.map(comidaDiaria, ComidaDiariaDTO.class);

        comidaDiariaDTO.setNombrePlanAlimenticio(comidaDiaria.getPlanAlimenticio().getPlanObjetivo());

        return comidaDiariaDTO;
    }

    public ComidaDiaria toEntity(ComidaDiariaCreateDTO comidaDiariaCreateDTO) {
        return modelMapper.map(comidaDiariaCreateDTO, ComidaDiaria.class);
    }
}
