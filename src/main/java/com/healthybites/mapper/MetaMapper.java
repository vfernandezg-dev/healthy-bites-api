package com.healthybites.mapper;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.dto.SeguimientoDetailsDTO;
import com.healthybites.model.entity.Meta;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class MetaMapper {

    private final ModelMapper modelMapper;


    public MetaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public MetaDTO toDTO(Meta meta) {
        MetaDTO metaDTO = modelMapper.map(meta, MetaDTO.class);

        metaDTO.setClienteNombre(meta.getCliente().getNombre() + " " + meta.getCliente().getApellido());

        // Convertir la lista de seguimientos a seguimientos DTO
        if (meta.getSeguimiento() != null && !meta.getSeguimiento().isEmpty()) {
            metaDTO.setSeguimientos(
                    meta.getSeguimiento().stream()
                            .map(s -> modelMapper.map(s, SeguimientoDetailsDTO.class))
                            .collect(Collectors.toList())
            );
        } else {
            metaDTO.setSeguimientos(Collections.emptyList()); // En caso de no tener seguimientos
        }

        return metaDTO;
    }

    public Meta toEntity(MetaCreateDTO metaCreateDTO) {
        return modelMapper.map(metaCreateDTO, Meta.class);
    }

    public MetaDTO toCreateUpdateDTO(Meta meta) {
        return modelMapper.map(meta, MetaDTO.class);
    }
}
