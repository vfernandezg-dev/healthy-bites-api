package com.healthybites.service.impl;

import com.healthybites.dto.ComidaDiariaCreateUpdateDTO;
import com.healthybites.dto.ComidaDiariaDetailsDTO;
import com.healthybites.exception.ResourceNotFoundException;
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

@RequiredArgsConstructor
@Service
public class ComidaDiariaServiceImpl implements ComidaDiariaService {

    private final ComidaDiariaRepository comidaDiariaRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final ComidaDiariaMapper comidaDiariaMapper;

    @Override
    public List<ComidaDiariaDetailsDTO> getAll() {
        List<ComidaDiaria> comidasDiarias = comidaDiariaRepository.findAll();
        return comidasDiarias.stream()
                .map(comidaDiariaMapper::toDTO)
                .toList();
    }

    @Override
    public ComidaDiariaDetailsDTO findById(Integer id) {
        ComidaDiaria comidaDiaria = comidaDiariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + id + " no encontrada"));

        return comidaDiariaMapper.toDTO(comidaDiaria);
    }

    @Override
    public ComidaDiariaDetailsDTO create(ComidaDiariaCreateUpdateDTO comidaDiariaCreateUpdateDTO) {
        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(comidaDiariaCreateUpdateDTO.getPlanAlimenticioId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + comidaDiariaCreateUpdateDTO.getPlanAlimenticioId() + " no encontrado"));

        ComidaDiaria comidaDiaria = comidaDiariaMapper.toEntity(comidaDiariaCreateUpdateDTO);

        comidaDiaria.setPlanAlimenticio(planAlimenticio);
        comidaDiaria.setFechaCreacion(LocalDateTime.now());
        comidaDiaria.setFechaActualizacion(LocalDateTime.now());

        return comidaDiariaMapper.toDTO(comidaDiariaRepository.save(comidaDiaria));
    }

    @Override
    public ComidaDiariaDetailsDTO update(Integer id, ComidaDiariaCreateUpdateDTO updateComidaDiariaDTO) {
        ComidaDiaria comidaFromDb = comidaDiariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + id + " no encontrada"));

        PlanAlimenticio planAlimenticio = planAlimenticioRepository.findById(updateComidaDiariaDTO.getPlanAlimenticioId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + updateComidaDiariaDTO.getPlanAlimenticioId() + " no encontrado"));

        comidaFromDb.setCategoria(updateComidaDiariaDTO.getCategoria());
        comidaFromDb.setCalorias(updateComidaDiariaDTO.getCalorias());
        comidaFromDb.setNombreComida(updateComidaDiariaDTO.getNombreComida());
        comidaFromDb.setPlanAlimenticio(planAlimenticio);

        return comidaDiariaMapper.toDTO(comidaDiariaRepository.save(comidaFromDb));
    }

    @Override
    public void delete(Integer id) {
        ComidaDiaria comidaDiaria = comidaDiariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria con id " + id + " no encontrada"));

        comidaDiariaRepository.delete(comidaDiaria);
    }
}
