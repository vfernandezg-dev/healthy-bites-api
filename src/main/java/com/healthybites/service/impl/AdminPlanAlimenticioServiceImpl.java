package com.healthybites.service.impl;

import com.healthybites.dto.PlanAlimenticioCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioDetailsDTO;
import com.healthybites.exception.BadRequestException;
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
        List<PlanAlimenticio> planAlimenticios = planAlimenticioRepository.findAll();
        return planAlimenticios.stream()
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
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio no encontrado: "+id));
        return planAlimenticioMapper.toDetailsDTO(planAlimenticio);
    }

    @Transactional
    @Override
    public PlanAlimenticioDetailsDTO create(PlanAlimenticioCreateUpdateDTO planAlimenticioCreateUpdateDTO) {

        //Asigna el nutricionista antes de guardar
        Nutricionista nutricionista = nutricionistaRepository.findById(planAlimenticioCreateUpdateDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista no encontrado con Id: " + planAlimenticioCreateUpdateDTO.getNutricionistaId()));

        PlanAlimenticio planAlimenticio = planAlimenticioMapper.toEntity(planAlimenticioCreateUpdateDTO);

        planAlimenticio.setNutricionista(nutricionista);

        return planAlimenticioMapper.toDetailsDTO(planAlimenticioRepository.save(planAlimenticio));
    }

    @Transactional
    @Override
    public PlanAlimenticioDetailsDTO update(Integer id, PlanAlimenticioCreateUpdateDTO updatePlanAlimenticioDTO) {
        // Busca el plan alimenticio existente
        PlanAlimenticio plan =  planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio no encontrado: "+id));

        // Busca el nutricionista asociado
        Nutricionista nutricionista = nutricionistaRepository.findById(updatePlanAlimenticioDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista no encontrado con Id: " + updatePlanAlimenticioDTO.getNutricionistaId()));

        // Actualiza solo los campos necesarios
        plan.setPlanObjetivo(updatePlanAlimenticioDTO.getPlanObjetivo());
        plan.setDescripcion(updatePlanAlimenticioDTO.getDescripcion());
        plan.setDuracionDias(updatePlanAlimenticioDTO.getDuracionDias());
        plan.setEsGratis(updatePlanAlimenticioDTO.isEsGratis());
        plan.setNutricionista(nutricionista);  // Asigna el nutricionista actualizado

        // Guarda y devuelve el plan actualizado
        return planAlimenticioMapper.toDetailsDTO(planAlimenticioRepository.save(plan));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio no encontrado: "+id));
        planAlimenticioRepository.delete(planAlimenticio);
    }
}
