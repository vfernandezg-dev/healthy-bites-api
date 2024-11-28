package com.healthybites.mapper;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDTO;
import com.healthybites.model.entity.Contenido;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ContenidoMapper {
    private final ModelMapper modelMapper;

    public ContenidoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public ContenidoDTO toDTO(Contenido contenido) {
        ContenidoDTO contenidoDetailsDTO = modelMapper.map(contenido, ContenidoDTO.class);

        contenidoDetailsDTO.setNutricionistaNombre(contenido.getNutricionista().getNombre() + " " + contenido.getNutricionista().getApellido());

        return contenidoDetailsDTO;
    }

    public Contenido toEntity(ContenidoCreateUpdateDTO contenidoCreateUpdateDTO) {
        return modelMapper.map(contenidoCreateUpdateDTO, Contenido.class);
    }

    public void updateFromDTO(ContenidoCreateUpdateDTO contenidoCreateUpdateDTO, Contenido contenido) {
        modelMapper.map(contenidoCreateUpdateDTO, contenido);
    }
}
