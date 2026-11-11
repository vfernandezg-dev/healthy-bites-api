package com.healthybites.service.impl;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ComidaDiariaMapper;
import com.healthybites.model.entity.ComidaDiaria;
import com.healthybites.repository.ComidaDiariaRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.AdminComidaDiariaService;
import com.healthybites.service.ComidaDiariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminComidaDiariaServiceImpl implements AdminComidaDiariaService {

    private final ComidaDiariaRepository comidaDiariaRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final ComidaDiariaMapper comidaDiariaMapper;
    private final ComidaDiariaService comidaDiariaService;

    @Transactional(readOnly = true)
    @Override
    public List<ComidaDiariaDTO> getAll() {
        return comidaDiariaRepository.findAll().stream()
                .map(comidaDiariaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ComidaDiariaDTO> paginate(Pageable pageable) {
        return comidaDiariaRepository.findAll(pageable).map(comidaDiariaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public ComidaDiariaDTO findById(Integer id) {
        return comidaDiariaMapper.toDTO(getComidaOrThrow(id));
    }

    @Transactional
    @Override
    public ComidaDiariaDTO create(Integer planAlimenticioId, ComidaDiariaCreateDTO comidaDiariaCreateDTO) {
        if (!planAlimenticioRepository.existsById(planAlimenticioId)) {
            throw new ResourceNotFoundException("Plan alimenticio no encontrado con Id: " + planAlimenticioId);
        }
        // Reutiliza la logica del flujo normal (asigna el plan y las fechas)
        return comidaDiariaService.create(planAlimenticioId, comidaDiariaCreateDTO);
    }

    @Transactional
    @Override
    public ComidaDiariaDTO update(Integer id, Integer planAlimenticioId, ComidaDiariaCreateDTO updateComidaDiaria) {
        ComidaDiaria comidaDiariaFromDB = getComidaOrThrow(id);

        comidaDiariaFromDB.setNombreComida(updateComidaDiaria.getNombreComida());
        comidaDiariaFromDB.setCalorias(updateComidaDiaria.getCalorias());
        comidaDiariaFromDB.setCategoria(updateComidaDiaria.getCategoria());
        if (planAlimenticioId != null) {
            comidaDiariaFromDB.setPlanAlimenticio(planAlimenticioRepository.findById(planAlimenticioId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Plan alimenticio no encontrado con Id: " + planAlimenticioId)));
        }
        comidaDiariaFromDB.setFechaActualizacion(LocalDateTime.now());

        return comidaDiariaMapper.toDTO(comidaDiariaRepository.save(comidaDiariaFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        comidaDiariaRepository.delete(getComidaOrThrow(id));
    }

    private ComidaDiaria getComidaOrThrow(Integer id) {
        return comidaDiariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comida diaria no encontrada: " + id));
    }
}
