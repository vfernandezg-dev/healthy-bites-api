package com.healthybites.service.impl;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.RecompensaMapper;
import com.healthybites.model.entity.Recompensa;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.ContenidoRepository;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.repository.RachaRepository;
import com.healthybites.repository.RecompensaRepository;
import com.healthybites.service.AdminRecompensaService;
import com.healthybites.service.RachaService;
import com.healthybites.service.RecompensaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminRecompensaServiceImpl implements AdminRecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final ClienteRepository clienteRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final ContenidoRepository contenidoRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final RachaRepository rachaRepository;
    private final RecompensaMapper recompensaMapper;
    private final RecompensaService recompensaService;
    private final RachaService rachaService;

    @Transactional(readOnly = true)
    @Override
    public List<RecompensaDetailsDTO> getAll() {
        return recompensaRepository.findAll().stream()
                .map(recompensaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RecompensaDetailsDTO> paginate(Pageable pageable) {
        return recompensaRepository.findAll(pageable).map(recompensaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public RecompensaDetailsDTO findById(Integer id) {
        return recompensaMapper.toDTO(getRecompensaOrThrow(id));
    }

    @Transactional
    @Override
    public RecompensaDetailsDTO create(RecompensaCreateUpdateDTO recompensaDTO) {
        if (recompensaDTO.getNutricionistaId() == null) {
            throw new BadRequestException("El ID del nutricionista es obligatorio");
        }
        // Reutiliza la logica del flujo normal (nutricionista, contenido/plan opcional y fechas)
        return recompensaService.create(recompensaDTO);
    }

    @Transactional
    @Override
    public RecompensaDetailsDTO update(Integer id, RecompensaCreateUpdateDTO updateRecompensaDTO) {
        Recompensa recompensaFromDb = getRecompensaOrThrow(id);

        if (updateRecompensaDTO.getNutricionistaId() != null) {
            recompensaFromDb.setNutricionista(nutricionistaRepository.findById(updateRecompensaDTO.getNutricionistaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Nutricionista no encontrado: " + updateRecompensaDTO.getNutricionistaId())));
        }
        recompensaFromDb.setNombre(updateRecompensaDTO.getNombre());
        recompensaFromDb.setDescripcion(updateRecompensaDTO.getDescripcion());
        recompensaFromDb.setDiasRequeridos(updateRecompensaDTO.getDiasRequeridos());
        if (updateRecompensaDTO.getContenidoId() != null) {
            recompensaFromDb.setContenido(contenidoRepository.findById(updateRecompensaDTO.getContenidoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Contenido no encontrado: " + updateRecompensaDTO.getContenidoId())));
        }
        if (updateRecompensaDTO.getPlanAlimenticioId() != null) {
            recompensaFromDb.setPlanAlimenticio(planAlimenticioRepository.findById(updateRecompensaDTO.getPlanAlimenticioId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Plan alimenticio no encontrado: " + updateRecompensaDTO.getPlanAlimenticioId())));
        }
        recompensaFromDb.setFechaActualizacion(LocalDateTime.now());

        return recompensaMapper.toDTO(recompensaRepository.save(recompensaFromDb));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        recompensaRepository.delete(getRecompensaOrThrow(id));
    }

    // Asignar recompensa a cliente = crear la racha cliente <-> recompensa
    @Transactional
    @Override
    public RecompensaDetailsDTO asignarRecompensaACliente(Integer recompensaId, Integer clienteId) {
        getRecompensaOrThrow(recompensaId);
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + clienteId);
        }
        if (rachaRepository.existsByClienteIdAndRecompensaId(clienteId, recompensaId)) {
            throw new BadRequestException("El cliente ya tiene asignada esa recompensa");
        }
        return rachaService.addRecompensaToClient(clienteId, recompensaId);
    }

    private Recompensa getRecompensaOrThrow(Integer id) {
        return recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada: " + id));
    }
}
