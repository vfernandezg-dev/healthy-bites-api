package com.healthybites.mapper;

import com.healthybites.dto.ComidaDiariaCreateUpdateDTO;
import com.healthybites.dto.ComidaDiariaDetailsDTO;
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

    public ComidaDiariaDetailsDTO toDTO(ComidaDiaria comidaDiaria) {
        ComidaDiariaDetailsDTO comidaDiariaDetailsDTO = modelMapper.map(comidaDiaria, ComidaDiariaDetailsDTO.class);

        comidaDiariaDetailsDTO.setNombrePlanAlimenticio(comidaDiaria.getPlanAlimenticio().getPlanObjetivo().toString());

        return comidaDiariaDetailsDTO;
    }

    public ComidaDiaria toEntity(ComidaDiariaCreateUpdateDTO comidaDiariaCreateUpdateDTO) {
        return modelMapper.map(comidaDiariaCreateUpdateDTO, ComidaDiaria.class);
    }

    public ContenidoCreateUpdateDTO toCreateUpdateDTO(ComidaDiaria comidaDiaria) {
        return modelMapper.map(comidaDiaria, ContenidoCreateUpdateDTO.class);
    }
}
