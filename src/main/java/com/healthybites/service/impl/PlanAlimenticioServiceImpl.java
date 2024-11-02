package com.healthybites.service.impl;

import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.PlanAlimenticioMapper;
import com.healthybites.model.entity.Nutricionista;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.PlanAlimenticioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlanAlimenticioServiceImpl implements PlanAlimenticioService {

    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final PlanAlimenticioMapper planAlimenticioMapper;

    @Override
    public List<PlanAlimenticioDTO> getAll(Integer id) {
        List<PlanAlimenticio> planAlimenticios = planAlimenticioRepository.findByNutricionistaId(id);

        return planAlimenticios.stream()
                .map(planAlimenticioMapper::toDTO)
                .toList();
    }

    @Override
    public PlanAlimenticioDTO findByIdAndNutricionistaId(Integer planId, Integer nutricionistaId) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findByIdAndNutricionistaId(planId, nutricionistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " y nutricionistaId " + nutricionistaId + " no encontrado"));
        return planAlimenticioMapper.toDTO(planAlimenticio);
    }

    @Override
    public PlanAlimenticioDTO create(PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        Nutricionista nutricionista = nutricionistaRepository.findById(planAlimenticioCreateDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista con id " + planAlimenticioCreateDTO.getNutricionistaId() + " no encontrado"));

        // Mapear el DTO a la entidad PlanAlimenticio
        PlanAlimenticio planAlimenticio = planAlimenticioMapper.toEntity(planAlimenticioCreateDTO);

        // Setear los campos del plan
        planAlimenticio.setPlanObjetivo(planAlimenticioCreateDTO.getPlanObjetivo());
        planAlimenticio.setDescripcion(planAlimenticioCreateDTO.getDescripcion());
        planAlimenticio.setDuracionDias(planAlimenticioCreateDTO.getDuracionDias());
        planAlimenticio.setEsGratis(planAlimenticioCreateDTO.isEsGratis());
        planAlimenticio.setNutricionista(nutricionista);

        // Seteamos las calorias totales
        planAlimenticio.setComidasDiarias(new ArrayList<>()); // Plan sin comidas inicialmente


        // Seteamos fechas
        planAlimenticio.setFechaCreacion(LocalDateTime.now());
        planAlimenticio.setFechaActualizacion(LocalDateTime.now());

        // Guardamos en la BD y devolvemos el DTO
        return planAlimenticioMapper.toDTO(planAlimenticioRepository.save(planAlimenticio));
    }

    @Override
    public PlanAlimenticioDTO update(Integer planId, Integer nutricionistaId, PlanAlimenticioCreateDTO updatedPlanAlimenticioDTO) {
        PlanAlimenticio planAlimenticioFromDb = planAlimenticioRepository.findByIdAndNutricionistaId(planId, nutricionistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " y nutricionistaId " + nutricionistaId + " no encontrado"));

        planAlimenticioFromDb.setPlanObjetivo(updatedPlanAlimenticioDTO.getPlanObjetivo());
        planAlimenticioFromDb.setDescripcion(updatedPlanAlimenticioDTO.getDescripcion());
        planAlimenticioFromDb.setDuracionDias(updatedPlanAlimenticioDTO.getDuracionDias());
        planAlimenticioFromDb.setEsGratis(updatedPlanAlimenticioDTO.isEsGratis());

        planAlimenticioFromDb.setFechaActualizacion(LocalDateTime.now());

        PlanAlimenticio updatedPlanAlimenticio = planAlimenticioRepository.save(planAlimenticioFromDb);

        return planAlimenticioMapper.toDTO(updatedPlanAlimenticio);
    }

    @Override
    public void delete(Integer planId, Integer nutricionistaId) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findByIdAndNutricionistaId(planId, nutricionistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " y nutricionistaId " + nutricionistaId + " no encontrado"));
        planAlimenticioRepository.delete(planAlimenticio);
    }

}
