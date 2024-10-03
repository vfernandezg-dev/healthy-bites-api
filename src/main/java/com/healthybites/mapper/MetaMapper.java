package com.healthybites.mapper;

import com.healthybites.dto.MetaDTO;
import com.healthybites.model.entity.Meta;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MetaMapper {
    private final ModelMapper modelMapper;

    public MetaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public MetaDTO toMetaDTO(Meta meta) {
        return modelMapper.map(meta, MetaDTO.class);
    }

    public Meta toEntity(MetaDTO metaDTO) {
        return modelMapper.map(metaDTO, Meta.class);
    }
}
