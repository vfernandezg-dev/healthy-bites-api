package com.healthybites.mapper;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import com.healthybites.model.entity.Publicacion;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class PublicacionMapper {

    private final ModelMapper modelMapper;

    public PublicacionMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public PublicacionDTO toDTO(Publicacion publicacion) {
        PublicacionDTO publicacionDTO = modelMapper.map(publicacion, PublicacionDTO.class);

        publicacionDTO.setNombreCliente(publicacion.getCliente().getNombre() + " " + publicacion.getCliente().getApellido());
        publicacionDTO.setNombreGrupo(publicacion.getGrupo().getNombre());

        return publicacionDTO;
    }

    public Publicacion toEntity(PublicacionCreateDTO publicacionCreateDTO) {
        return modelMapper.map(publicacionCreateDTO, Publicacion.class);
    }

    public void updateFromDTO(PublicacionCreateDTO publicacionCreateDTO, Publicacion publicacion) {
        modelMapper.map(publicacionCreateDTO, publicacion);
    }
}
