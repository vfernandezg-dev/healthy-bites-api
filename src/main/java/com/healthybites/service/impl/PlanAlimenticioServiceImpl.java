package com.healthybites.service.impl;

import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.NutricionistaMapper;
import com.healthybites.mapper.PlanAlimenticioMapper;
import com.healthybites.model.entity.ComidaDiaria;
import com.healthybites.model.entity.Nutricionista;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.ComidaDiariaRepository;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.PlanAlimenticioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlanAlimenticioServiceImpl implements PlanAlimenticioService {

    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final ComidaDiariaRepository comidaDiariaRepository;
    private final PlanAlimenticioMapper planAlimenticioMapper;

    @Override
    public List<PlanAlimenticioDTO> getAll() {
        List<PlanAlimenticio> planAlimenticios = planAlimenticioRepository.findAll();
        return planAlimenticios.stream()
                .map(planAlimenticioMapper::toDTO)
                .toList();
    }

    @Override
    public PlanAlimenticioDTO findById(Integer id) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + id + " no encontrado"));
        return planAlimenticioMapper.toDTO(planAlimenticio);
    }

    @Override
    public PlanAlimenticioDTO create(PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {

        Nutricionista nutricionista = nutricionistaRepository.findById(planAlimenticioCreateDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista con id " + planAlimenticioCreateDTO.getNutricionistaId() + " no encontrado"));

        List<ComidaDiaria> comidasDiarias = planAlimenticioCreateDTO.getComidasDiariasIds().stream()
                .map(id -> comidaDiariaRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + id + " no encontrada")))
                .collect(Collectors.toList());

        PlanAlimenticio planAlimenticio = planAlimenticioMapper.toEntity(planAlimenticioCreateDTO);

        planAlimenticio.setPlanObjetivo(planAlimenticioCreateDTO.getPlanObjetivo());
        planAlimenticio.setDescripcion(planAlimenticioCreateDTO.getDescripcion());
        planAlimenticio.setDuracionDias(planAlimenticioCreateDTO.getDuracionDias());
        planAlimenticio.setEsGratis(planAlimenticioCreateDTO.isEsGratis());
        planAlimenticio.setNutricionista(nutricionista);
        planAlimenticio.setComidasDiarias(comidasDiarias);

        planAlimenticio.setFechaCreacion(LocalDateTime.now());
        planAlimenticio.setFechaActualizacion(LocalDateTime.now());

        return planAlimenticioMapper.toDTO(planAlimenticioRepository.save(planAlimenticio));
    }

    @Override
    public PlanAlimenticioDTO update(Integer id, PlanAlimenticioCreateDTO updatedPlanAlimenticioDTO) {
        PlanAlimenticio planAlimenticioFromDb = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + id + " no encontrado"));

        Nutricionista nutricionista = nutricionistaRepository.findById(updatedPlanAlimenticioDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista con id " + updatedPlanAlimenticioDTO.getNutricionistaId() + " no encontrado"));

        List<ComidaDiaria> comidasDiarias = updatedPlanAlimenticioDTO.getComidasDiariasIds().stream()
                .map(comidaId -> comidaDiariaRepository.findById(comidaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + comidaId + " no encontrada")))
                .collect(Collectors.toList());

        planAlimenticioFromDb.setPlanObjetivo(updatedPlanAlimenticioDTO.getPlanObjetivo());
        planAlimenticioFromDb.setDescripcion(updatedPlanAlimenticioDTO.getDescripcion());
        planAlimenticioFromDb.setDuracionDias(updatedPlanAlimenticioDTO.getDuracionDias());
        planAlimenticioFromDb.setEsGratis(updatedPlanAlimenticioDTO.isEsGratis());
        planAlimenticioFromDb.setNutricionista(nutricionista);
        planAlimenticioFromDb.setComidasDiarias(comidasDiarias);

        planAlimenticioFromDb.setFechaActualizacion(LocalDateTime.now());

        PlanAlimenticio updatedPlanAlimenticio = planAlimenticioRepository.save(planAlimenticioFromDb);

        return planAlimenticioMapper.toDTO(updatedPlanAlimenticio);
    }

    @Override
    public void delete(Integer id) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + id + " no encontrado"));
        planAlimenticioRepository.delete(planAlimenticio);
    }
}
