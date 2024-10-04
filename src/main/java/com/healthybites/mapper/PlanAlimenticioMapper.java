package com.healthybites.mapper;

import com.healthybites.dto.PlanAlimenticioCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioDetailsDTO;
import com.healthybites.model.entity.PlanAlimenticio;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class PlanAlimenticioMapper {
    private final ModelMapper modelMapper;

    public PlanAlimenticioMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public PlanAlimenticioDetailsDTO toDetailsDTO(PlanAlimenticio planAlimenticio) {
        PlanAlimenticioDetailsDTO planAlimenticioDetailsDTO = modelMapper.map(planAlimenticio, PlanAlimenticioDetailsDTO.class);
        planAlimenticioDetailsDTO.setNutricionistaNombre(planAlimenticio.getNutricionista().getNombre() + " " + planAlimenticio.getNutricionista().getApellido());
        return planAlimenticioDetailsDTO;
    }

    public PlanAlimenticio toEntity(PlanAlimenticioCreateUpdateDTO planAlimenticioCreateUpdateDTO) {
        return modelMapper.map(planAlimenticioCreateUpdateDTO, PlanAlimenticio.class);
    }

    public PlanAlimenticioCreateUpdateDTO toCreateUpdateDTO(PlanAlimenticio planAlimenticio) {
        return modelMapper.map(planAlimenticio, PlanAlimenticioCreateUpdateDTO.class);
    }


}
