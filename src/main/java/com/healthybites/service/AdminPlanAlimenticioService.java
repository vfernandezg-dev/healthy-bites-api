package com.healthybites.service;

import com.healthybites.dto.PlanAlimenticioCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioDetailsDTO;
import com.healthybites.model.entity.PlanAlimenticio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminPlanAlimenticioService {
    List<PlanAlimenticioDetailsDTO> getAll();
    Page<PlanAlimenticioDetailsDTO> paginate(Pageable pageable);
    PlanAlimenticioDetailsDTO findById(Integer id);
    PlanAlimenticioDetailsDTO create(PlanAlimenticioCreateUpdateDTO planAlimenticioCreateUpdateDTO);
    PlanAlimenticioDetailsDTO update(Integer id, PlanAlimenticioCreateUpdateDTO updatePlanAlimenticioDTO);
    void delete(Integer id);
}