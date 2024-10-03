package com.healthybites.service.impl;

import com.healthybites.model.entity.ComidaDiaria;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.ComidaDiariaRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.AdminComidaDiariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminComidaDiariaServiceImpl implements AdminComidaDiariaService {

    private final ComidaDiariaRepository comidaDiariaRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ComidaDiaria> getAll() {
        return comidaDiariaRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ComidaDiaria> paginate(Pageable pageable) {
        return comidaDiariaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public ComidaDiaria findById(Integer id) {
        return comidaDiariaRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Comida diaria no encontrada"));
    }

    @Transactional
    @Override
    public ComidaDiaria create(ComidaDiaria comidaDiaria) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(comidaDiaria.getPlanAlimenticio().getId())
                .orElseThrow(() -> new RuntimeException("Plan alimenticio no encontrado con Id: " + comidaDiaria.getPlanAlimenticio().getId()));
        return comidaDiariaRepository.save(comidaDiaria);
    }

    @Transactional
    @Override
    public ComidaDiaria update(Integer id, ComidaDiaria updateComidaDiaria) {
        ComidaDiaria comidaDiariaFromDB = findById(id);
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(updateComidaDiaria.getPlanAlimenticio().getId())
                .orElseThrow(() -> new RuntimeException("Plan alimenticio no encontrado con Id: " + updateComidaDiaria.getPlanAlimenticio().getId()));

        comidaDiariaFromDB.setNombreComida(updateComidaDiaria.getNombreComida());
        comidaDiariaFromDB.setCalorias(updateComidaDiaria.getCalorias());
        comidaDiariaFromDB.setCategoria(updateComidaDiaria.getCategoria());
        comidaDiariaFromDB.setPlanAlimenticio(planAlimenticio);
        return comidaDiariaRepository.save(comidaDiariaFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        ComidaDiaria comidaDiaria = findById(id);
        comidaDiariaRepository.delete(comidaDiaria);
    }
}
