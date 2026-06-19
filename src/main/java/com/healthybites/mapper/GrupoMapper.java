package com.healthybites.mapper;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.model.entity.Grupo;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class GrupoMapper {

    private final ModelMapper modelMapper;

    public GrupoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public GrupoDTO toDTO(Grupo grupo) {
        GrupoDTO grupoDTO = modelMapper.map(grupo, GrupoDTO.class);

        grupoDTO.setClienteNombre(grupo.getCliente().getNombre() + " " + grupo.getCliente().getApellido());

        return grupoDTO;
    }

    public Grupo toEntity(GrupoCreateDTO grupoCreateDTO) {
        return modelMapper.map(grupoCreateDTO, Grupo.class);
    }

    public void updateFromDTO(GrupoCreateDTO grupoCreateDTO, Grupo grupo) {
        modelMapper.map(grupoCreateDTO, grupo);
    }
}
