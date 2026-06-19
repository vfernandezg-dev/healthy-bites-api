package com.healthybites.mapper;

import com.healthybites.dto.ContenidoDTO;
import com.healthybites.model.entity.Contenido;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ContenidoMapper {

    private final ModelMapper modelMapper;

    public ContenidoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ContenidoDTO toDto(Contenido contenido) {
        // Convertir la entidad Contenido al DTO
        return modelMapper.map(contenido, ContenidoDTO.class);
    }

    public Contenido toEntity(ContenidoDTO contenidoDTO) {
        // Convertir el DTO en la entidad Contenido
        return modelMapper.map(contenidoDTO, Contenido.class);
    }
}

