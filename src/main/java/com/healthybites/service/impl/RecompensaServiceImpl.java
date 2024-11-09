package com.healthybites.service.impl;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.RecompensaMapper;
import com.healthybites.model.entity.*;
import com.healthybites.repository.*;
import com.healthybites.service.RecompensaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RecompensaServiceImpl implements RecompensaService {

    private final ContenidoRepository contenidoRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final RecompensaRepository recompensaRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final RecompensaMapper recompensaMapper;

    @Override
    public List<RecompensaDetailsDTO> getAll() {
        List<Recompensa> recompensas = recompensaRepository.findAll();
        return recompensas.stream()
                .map(recompensaMapper::toDTO)
                .toList();
    }

    @Override
    public RecompensaDetailsDTO findById(Integer id) {
        Recompensa recompensa = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa con id " + id + " no encontrada"));

        return recompensaMapper.toDTO(recompensa);
    }

    @Override
    public RecompensaDetailsDTO create(RecompensaCreateUpdateDTO recompensaCreateUpdateDTO) {
        Nutricionista nutricionista = nutricionistaRepository.findById(recompensaCreateUpdateDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista con id " + recompensaCreateUpdateDTO.getNutricionistaId() + " no encontrado"));

        Recompensa recompensa = recompensaMapper.toEntity(recompensaCreateUpdateDTO);

        // Asignar contenido o plan si los IDs están presentes
        if (recompensaCreateUpdateDTO.getContenidoId() != null) {
            Contenido contenido = contenidoRepository.findById(recompensaCreateUpdateDTO.getContenidoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contenido con id " + recompensaCreateUpdateDTO.getContenidoId() + " no encontrado"));
            recompensa.setContenido(contenido);
        } else if (recompensaCreateUpdateDTO.getPlanAlimenticioId() != null) {
            PlanAlimenticio plan = planAlimenticioRepository.findById(recompensaCreateUpdateDTO.getPlanAlimenticioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + recompensaCreateUpdateDTO.getPlanAlimenticioId() + " no encontrado"));
            recompensa.setPlanAlimenticio(plan);
        }

        recompensa.setNutricionista(nutricionista);
        recompensa.setFechaCreacion(LocalDateTime.now());
        recompensa.setFechaActualizacion(LocalDateTime.now());

        return recompensaMapper.toDTO(recompensaRepository.save(recompensa));
    }

    @Override
    public RecompensaDetailsDTO update(Integer id, RecompensaCreateUpdateDTO updateRecompensaDTO) {
        Recompensa recompensaFromDb = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa con id " + id + " no encontrada"));

        Nutricionista nutricionista = nutricionistaRepository.findById(updateRecompensaDTO.getNutricionistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista con id " + updateRecompensaDTO.getNutricionistaId() + " no encontrado"));

        recompensaFromDb.setNombre(updateRecompensaDTO.getNombre());
        recompensaFromDb.setDescripcion(updateRecompensaDTO.getDescripcion());
        recompensaFromDb.setDiasRequeridos(updateRecompensaDTO.getDiasRequeridos());
        recompensaFromDb.setNutricionista(nutricionista);

        // Actualizar contenido o plan si se proporcionan IDs
        if (updateRecompensaDTO.getContenidoId() != null) {
            Contenido contenido = contenidoRepository.findById(updateRecompensaDTO.getContenidoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contenido con id " + updateRecompensaDTO.getContenidoId() + " no encontrado"));
            recompensaFromDb.setContenido(contenido);
        } else if (updateRecompensaDTO.getPlanAlimenticioId() != null) {
            PlanAlimenticio plan = planAlimenticioRepository.findById(updateRecompensaDTO.getPlanAlimenticioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan alimenticio con id " + updateRecompensaDTO.getPlanAlimenticioId() + " no encontrado"));
            recompensaFromDb.setPlanAlimenticio(plan);
        }


        recompensaFromDb.setFechaActualizacion(LocalDateTime.now());

        return recompensaMapper.toDTO(recompensaRepository.save(recompensaFromDb));
    }


    @Override
    public void delete(Integer id) {
        Recompensa recompensa = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa con id " + id + " no encontrada"));

        recompensaRepository.delete(recompensa);
    }

}
