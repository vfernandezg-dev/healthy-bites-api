package com.healthybites.mapper;

import com.healthybites.dto.NutricionistaDTO;
import com.healthybites.model.entity.Nutricionista;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NutricionistaMapper {

    private ModelMapper modelMapper;

    public NutricionistaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public NutricionistaDTO toDto(Nutricionista nutricionista) {
        return modelMapper.map(nutricionista, NutricionistaDTO.class);
    }

    public Nutricionista toEntity(NutricionistaDTO nutricionistaDTO) {
        return modelMapper.map(nutricionistaDTO, Nutricionista.class);
    }

}
