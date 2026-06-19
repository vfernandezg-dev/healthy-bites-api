package com.healthybites.service;

import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;

import java.util.List;

public interface PlanAlimenticioService {
    List<PlanAlimenticioDTO> getAll();
    PlanAlimenticioDTO findById(Integer id);
    PlanAlimenticioDTO create(PlanAlimenticioCreateDTO planAlimenticioCreateDTO);
    PlanAlimenticioDTO update(Integer id, PlanAlimenticioCreateDTO updatedPlanAlimenticioDTO);
    void delete(Integer id);
}
