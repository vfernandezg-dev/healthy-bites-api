package com.healthybites.service.impl;

import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ComidaDiariaMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlanAlimenticioServiceImpl implements PlanAlimenticioService {

    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final ComidaDiariaRepository comidaDiariaRepository;
    private final PlanAlimenticioMapper planAlimenticioMapper;
    private final ComidaDiariaMapper comidaDiariaMapper;

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

    //-----------------------------------------------------------------------------------------------------------

    /*
    @Override
    public List<ComidaDiariaDTO> getAllComidasByPlanId(Integer planId) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " no encontrado"));

        // Recuperar todas las comidas diarias asociadas al plan
        List<ComidaDiaria> comidasDiarias = planAlimenticio.getComidasDiarias();

        // Mapear a DTOs
        return comidasDiarias.stream()
                .map(comidaDiariaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PlanAlimenticioDTO findById(Integer id) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + id + " no encontrado"));
        return planAlimenticioMapper.toDTO(planAlimenticio);
    }

    @Override
    public ComidaDiariaDTO addComidaToPlan(Integer planId, ComidaDiariaDTO comidaDiariaDTO) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " no encontrado"));

        ComidaDiaria comidaDiaria = comidaDiariaMapper.toEntity(comidaDiariaDTO);
        comidaDiaria.setNombreComida(comidaDiariaDTO.getNombreComida());
        comidaDiaria.setCategoria(comidaDiariaDTO.getCategoria());
        comidaDiaria.setCalorias(comidaDiariaDTO.getCalorias());
        comidaDiaria.setFechaCreacion(LocalDateTime.now());
        comidaDiaria.setFechaActualizacion(LocalDateTime.now());

        // Asociar comida al plan
        comidaDiaria.setPlanAlimenticio(planAlimenticio);

        // Guardar comida en base de datos
        comidaDiaria = comidaDiariaRepository.save(comidaDiaria);  // <- Aquí se guarda y se genera el ID

        // Retornar DTO de la comida guardada
        return comidaDiariaMapper.toDTO(comidaDiaria);
    }

    @Override
    public ComidaDiariaDTO getComidaById(Integer planId, Integer comidaId) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " no encontrado"));

        ComidaDiaria comidaDiaria = planAlimenticio.getComidasDiarias().stream()
                .filter(c -> c.getId().equals(comidaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + comidaId + " no encontrada"));

        return comidaDiariaMapper.toDTO(comidaDiaria);
    }

    @Override
    public ComidaDiariaDTO updateComida(Integer planId, Integer comidaId, ComidaDiariaDTO comidaDiariaDTO) {
        // Buscar el plan por su id
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " no encontrado"));

        // Buscar la comida por su id
        ComidaDiaria comidaDiaria = comidaDiariaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + comidaId + " no encontrada"));

        // Verificar que la comida pertenezca al plan alimenticio correcto
        if (!comidaDiaria.getPlanAlimenticio().getId().equals(planId)) {
            throw new IllegalArgumentException("La comida no pertenece a este plan alimenticio");
        }

        // Actualizar los campos de la comida
        comidaDiaria.setNombreComida(comidaDiariaDTO.getNombreComida());
        comidaDiaria.setCalorias(comidaDiariaDTO.getCalorias());
        comidaDiaria.setCategoria(comidaDiariaDTO.getCategoria());
        comidaDiaria.setFechaActualizacion(LocalDateTime.now());

        // Guardar la comida actualizada
        comidaDiariaRepository.save(comidaDiaria);

        // Retornar el DTO actualizado
        return comidaDiariaMapper.toDTO(comidaDiaria);
    }

    @Override
    public void deleteComida(Integer planId, Integer comidaId) {
        // Buscar el plan alimenticio por su id
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + planId + " no encontrado"));

        // Buscar la comida por su id
        ComidaDiaria comidaDiaria = comidaDiariaRepository.findById(comidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + comidaId + " no encontrada"));

        // Verificar que la comida pertenezca al plan alimenticio
        if (!comidaDiaria.getPlanAlimenticio().getId().equals(planId)) {
            throw new IllegalArgumentException("La comida no pertenece a este plan alimenticio");
        }

        // Eliminar la comida del plan alimenticio
        planAlimenticio.getComidasDiarias().remove(comidaDiaria);

        // Guardar el plan alimenticio sin la comida
        planAlimenticioRepository.save(planAlimenticio);

        // Eliminar la comida de la base de datos
        comidaDiariaRepository.delete(comidaDiaria);
    }
    */

}
