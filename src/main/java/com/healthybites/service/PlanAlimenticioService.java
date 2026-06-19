package com.healthybites.service;

import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;

import java.util.List;

public interface PlanAlimenticioService {

    List<PlanAlimenticioDTO> getAll(Integer id);
    PlanAlimenticioDTO findByIdAndNutricionistaId(Integer planId, Integer nutricionistaId);
    PlanAlimenticioDTO create(PlanAlimenticioCreateDTO planAlimenticioCreateDTO);
    PlanAlimenticioDTO update(Integer planId, Integer nutricionistaId, PlanAlimenticioCreateDTO updatedPlanAlimenticioDTO);
    void delete(Integer planId, Integer nutricionistaId);
}
