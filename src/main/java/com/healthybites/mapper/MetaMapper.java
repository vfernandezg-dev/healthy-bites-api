package com.healthybites.mapper;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.model.entity.Meta;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

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
        metaDTO.setMetaId(meta.getId());

        return metaDTO;
    }

    public Meta toEntity(MetaCreateDTO metaCreateDTO) {
        return modelMapper.map(metaCreateDTO, Meta.class);
    }

    public void updateFromDTO(MetaCreateDTO metaCreateDTO, Meta meta) {
        modelMapper.map(metaCreateDTO, meta);
    }
}
