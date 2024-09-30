package com.healthybites.service;

import com.healthybites.model.entity.PlanAlimenticio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminPlanAlimenticioService {
    List<PlanAlimenticio> getAll();
    Page<PlanAlimenticio> paginate(Pageable pageable);
    PlanAlimenticio findById(Integer id);
    PlanAlimenticio create(PlanAlimenticio planAlimenticio);
    PlanAlimenticio update(Integer id, PlanAlimenticio updatePlanAlimenticio);
    void delete(Integer id);
}
