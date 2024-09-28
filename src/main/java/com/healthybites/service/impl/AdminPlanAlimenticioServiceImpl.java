package com.healthybites.service.impl;

import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.AdminPlanAlimenticioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminPlanAlimenticioServiceImpl implements AdminPlanAlimenticioService {

    private final PlanAlimenticioRepository planAlimenticioRepository;

    @Transactional(readOnly = true)
    @Override
    public List<PlanAlimenticio> getAll() {
        return planAlimenticioRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PlanAlimenticio> paginate(Pageable pageable) {
        return planAlimenticioRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public PlanAlimenticio findById(Integer id) {
        return planAlimenticioRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Plan alimenticio no encontrado"));
    }

    @Transactional
    @Override
    public PlanAlimenticio create(PlanAlimenticio planAlimenticio) {
        return planAlimenticioRepository.save(planAlimenticio);
    }

    @Transactional
    @Override
    public PlanAlimenticio update(Integer id, PlanAlimenticio updatePlanAlimenticio) {
        PlanAlimenticio planAlimenticioFromDB = findById(id);
        planAlimenticioFromDB.setPlanObjetivo(updatePlanAlimenticio.getPlanObjetivo());
        planAlimenticioFromDB.setDescripcion(updatePlanAlimenticio.getDescripcion());
        planAlimenticioFromDB.setDuracionDias(updatePlanAlimenticio.getDuracionDias());
        planAlimenticioFromDB.setEsGratis(updatePlanAlimenticio.isEsGratis());
        return planAlimenticioRepository.save(planAlimenticioFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        PlanAlimenticio planAlimenticio = findById(id);
        planAlimenticioRepository.delete(planAlimenticio);
    }
}
