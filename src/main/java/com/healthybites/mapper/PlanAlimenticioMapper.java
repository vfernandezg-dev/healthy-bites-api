package com.healthybites.mapper;

import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.model.entity.ComidaDiaria;
import com.healthybites.model.entity.PlanAlimenticio;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PlanAlimenticioMapper {

    private final ModelMapper modelMapper;

    public PlanAlimenticioMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public PlanAlimenticioDTO toDTO(PlanAlimenticio planAlimenticio) {
        PlanAlimenticioDTO planAlimenticioDTO = modelMapper.map(planAlimenticio, PlanAlimenticioDTO.class);

        planAlimenticioDTO.setNutricionistaNombre(planAlimenticio.getNutricionista().getNombre() + " " + planAlimenticio.getNutricionista().getApellido());
        planAlimenticioDTO.setCaloriasTotales(planAlimenticio.getComidasDiarias().stream().mapToInt(ComidaDiaria::getCalorias).sum());

        return planAlimenticioDTO;
    }

    public PlanAlimenticio toEntity(PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        return modelMapper.map(planAlimenticioCreateDTO, PlanAlimenticio.class);
    }

    public void updateFromDTO(PlanAlimenticioCreateDTO planAlimenticioCreateDTO, PlanAlimenticio planAlimenticio) {
        modelMapper.map(planAlimenticioCreateDTO, planAlimenticio);
    }
}
