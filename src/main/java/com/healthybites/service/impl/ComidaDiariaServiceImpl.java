package com.healthybites.service.impl;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.mapper.ComidaDiariaMapper;
import com.healthybites.model.entity.ComidaDiaria;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.ComidaDiariaRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.ComidaDiariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ComidaDiariaServiceImpl implements ComidaDiariaService {

    private final ComidaDiariaRepository comidaDiariaRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final ComidaDiariaMapper comidaDiariaMapper;

    @Override
    public List<ComidaDiariaDTO> getAll(Integer planId) {
        List<ComidaDiaria> comidaDiarias = comidaDiariaRepository.findByPlanAlimenticioId(planId);

        return comidaDiarias.stream()
                .map(comidaDiariaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComidaDiariaDTO findByIdAndPlanId(Integer comidaDiariaId, Integer planId) {
        ComidaDiaria comidaDiaria = comidaDiariaRepository.findByIdAndPlanAlimenticioId(comidaDiariaId, planId)
                .orElseThrow(() -> new RuntimeException("Comida diaria no encontrada"));
        return comidaDiariaMapper.toDTO(comidaDiaria);
    }

    @Override
    public ComidaDiariaDTO create(Integer planId, ComidaDiariaCreateDTO comidaDiariaCreateDTO) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan alimenticio no encontrado"));

        ComidaDiaria comidaDiaria = comidaDiariaMapper.toEntity(comidaDiariaCreateDTO);

        comidaDiaria.setPlanAlimenticio(planAlimenticio);

        comidaDiaria.setFechaCreacion(LocalDateTime.now());
        comidaDiaria.setFechaActualizacion(LocalDateTime.now());

        return comidaDiariaMapper.toDTO(comidaDiariaRepository.save(comidaDiaria));
    }

    @Override
    public ComidaDiariaDTO update(Integer planId, Integer comidaDiariaId, ComidaDiariaCreateDTO updatedComidaDiariaDTO) {
        ComidaDiaria comidaDiariaFromDb = comidaDiariaRepository.findByIdAndPlanAlimenticioId(comidaDiariaId, planId)
                .orElseThrow(() -> new RuntimeException("Comida con id " + comidaDiariaId + " y Plan Alimenticio con Id " + planId + " no encontrada"));

        comidaDiariaMapper.updateFromDTO(updatedComidaDiariaDTO, comidaDiariaFromDb);

        comidaDiariaFromDb.setFechaActualizacion(LocalDateTime.now());

        return comidaDiariaMapper.toDTO(comidaDiariaRepository.save(comidaDiariaFromDb));
    }

    @Override
    public void delete(Integer planId, Integer comidaDiariaId) {
        ComidaDiaria comidaDiaria = comidaDiariaRepository.findByIdAndPlanAlimenticioId(comidaDiariaId, planId)
                .orElseThrow(() -> new RuntimeException("Comida diaria no encontrada"));
        comidaDiariaRepository.delete(comidaDiaria);
    }
}
