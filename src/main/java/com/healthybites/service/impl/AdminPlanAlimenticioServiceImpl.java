package com.healthybites.service.impl;

import com.healthybites.model.entity.Nutricionista;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.NutricionistaRepository;
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
    private final NutricionistaRepository nutricionistaRepository;

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
        //Asigna el nutricionista antes de guardar
        Nutricionista nutricionista = nutricionistaRepository.findById(planAlimenticio.getNutricionista().getId())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado con Id: " + planAlimenticio.getNutricionista().getId()));

        planAlimenticio.setNutricionista(nutricionista);

        return planAlimenticioRepository.save(planAlimenticio);
    }

    @Transactional
    @Override
    public PlanAlimenticio update(Integer id, PlanAlimenticio updatePlanAlimenticio) {

        PlanAlimenticio planAlimenticioFromDB = findById(id);

        //Asigna el nutricionista antes de actualizar
        Nutricionista nutricionista = nutricionistaRepository.findById(updatePlanAlimenticio.getNutricionista().getId())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado con Id: " + updatePlanAlimenticio.getNutricionista().getId()));


        planAlimenticioFromDB.setPlanObjetivo(updatePlanAlimenticio.getPlanObjetivo());
        planAlimenticioFromDB.setDescripcion(updatePlanAlimenticio.getDescripcion());
        planAlimenticioFromDB.setDuracionDias(updatePlanAlimenticio.getDuracionDias());
        planAlimenticioFromDB.setEsGratis(updatePlanAlimenticio.isEsGratis());
        planAlimenticioFromDB.setNutricionista(nutricionista);
        return planAlimenticioRepository.save(planAlimenticioFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan alimenticio no encontrado: "+id));
        planAlimenticioRepository.delete(planAlimenticio);
    }
}
