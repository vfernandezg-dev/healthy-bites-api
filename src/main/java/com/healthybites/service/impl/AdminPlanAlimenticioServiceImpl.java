package com.healthybites.service.impl;

import com.healthybites.dto.PlanAlimenticioCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioDetailsDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.PlanAlimenticioMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminPlanAlimenticioServiceImpl implements AdminPlanAlimenticioService {

    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final PlanAlimenticioMapper planAlimenticioMapper;

    @Transactional(readOnly = true)
    @Override
    public List<PlanAlimenticioDetailsDTO> getAll() {
        return planAlimenticioRepository.findAll().stream()
                .map(planAlimenticioMapper::toDetailsDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PlanAlimenticioDetailsDTO> paginate(Pageable pageable) {
        return planAlimenticioRepository.findAll(pageable)
                .map(planAlimenticioMapper::toDetailsDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public PlanAlimenticioDetailsDTO findById(Integer id) {
        return planAlimenticioMapper.toDetailsDTO(getPlanOrThrow(id));
    }

    @Transactional
    @Override
    public PlanAlimenticioDetailsDTO create(PlanAlimenticioCreateUpdateDTO planAlimenticioCreateUpdateDTO) {
        Nutricionista nutricionista = getNutricionistaOrThrow(planAlimenticioCreateUpdateDTO.getNutricionistaId());

        PlanAlimenticio planAlimenticio = planAlimenticioMapper.toEntity(planAlimenticioCreateUpdateDTO);
        planAlimenticio.setId(null);
        planAlimenticio.setNutricionista(nutricionista);
        planAlimenticio.setComidasDiarias(new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();
        planAlimenticio.setFechaCreacion(ahora);
        planAlimenticio.setFechaActualizacion(ahora);

        return planAlimenticioMapper.toDetailsDTO(planAlimenticioRepository.save(planAlimenticio));
    }

    @Transactional
    @Override
    public PlanAlimenticioDetailsDTO update(Integer id, PlanAlimenticioCreateUpdateDTO updatePlanAlimenticioDTO) {
        PlanAlimenticio plan = getPlanOrThrow(id);
        Nutricionista nutricionista = getNutricionistaOrThrow(updatePlanAlimenticioDTO.getNutricionistaId());

        plan.setPlanObjetivo(updatePlanAlimenticioDTO.getPlanObjetivo());
        plan.setDescripcion(updatePlanAlimenticioDTO.getDescripcion());
        plan.setDuracionDias(updatePlanAlimenticioDTO.getDuracionDias());
        plan.setEsGratis(updatePlanAlimenticioDTO.isEsGratis());
        plan.setNutricionista(nutricionista);
        plan.setFechaActualizacion(LocalDateTime.now());

        return planAlimenticioMapper.toDetailsDTO(planAlimenticioRepository.save(plan));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        planAlimenticioRepository.delete(getPlanOrThrow(id));
    }

    private PlanAlimenticio getPlanOrThrow(Integer id) {
        return planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio no encontrado: " + id));
    }

    private Nutricionista getNutricionistaOrThrow(Integer nutricionistaId) {
        return nutricionistaRepository.findById(nutricionistaId)
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista no encontrado con Id: " + nutricionistaId));
    }
}
