package com.healthybites.mapper;

import com.healthybites.dto.ComentarioCreateDTO;
import com.healthybites.dto.ComentarioDTO;
import com.healthybites.model.entity.Comentario;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    private final ModelMapper modelMapper;

    public ComentarioMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public ComentarioDTO toDTO(Comentario comentario) {
        ComentarioDTO comentarioDTO = modelMapper.map(comentario, ComentarioDTO.class);

        comentarioDTO.setNombreCliente(comentario.getCliente().getNombre() + " " + comentario.getCliente().getApellido());
        comentarioDTO.setNombrePublicacion(comentario.getPublicacion().getTitulo());

        return comentarioDTO;
    }

    public Comentario toEntity(ComentarioCreateDTO comentarioCreateDTO) {
        return modelMapper.map(comentarioCreateDTO, Comentario.class);
    }

    public void updateFromDTO(ComentarioCreateDTO comentarioCreateDTO, Comentario comentario) {
        modelMapper.map(comentarioCreateDTO, comentario);
    }
}
