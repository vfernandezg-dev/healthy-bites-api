package com.healthybites.service.impl;

import com.healthybites.dto.RachaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.RachaMapper;
import com.healthybites.model.entity.Racha;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.RachaRepository;
import com.healthybites.repository.RecompensaRepository;
import com.healthybites.service.AdminRachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminRachaServiceImpl implements AdminRachaService {

    private final RachaRepository rachaRepository;
    private final ClienteRepository clienteRepository;
    private final RecompensaRepository recompensaRepository;
    private final RachaMapper rachaMapper;

    @Transactional(readOnly = true)
    @Override
    public List<RachaDTO> getAll() {
        return rachaRepository.findAll().stream()
                .map(rachaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RachaDTO> paginate(Pageable pageable) {
        return rachaRepository.findAll(pageable).map(rachaMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RachaDTO findById(Integer clienteId, Integer recompensaId) {
        return rachaMapper.toDto(getRachaOrThrow(clienteId, recompensaId));
    }

    @Transactional
    @Override
    public RachaDTO create(RachaDTO rachaDTO) {
        Integer clienteId = rachaDTO.getClienteId();
        Integer recompensaId = rachaDTO.getRecompensaId();
        if (clienteId == null || recompensaId == null) {
            throw new BadRequestException("clienteId y recompensaId son obligatorios");
        }
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + clienteId);
        }
        if (!recompensaRepository.existsById(recompensaId)) {
            throw new ResourceNotFoundException("Recompensa no encontrada: " + recompensaId);
        }
        if (rachaRepository.existsByClienteIdAndRecompensaId(clienteId, recompensaId)) {
            throw new BadRequestException("El cliente ya tiene una racha para esa recompensa");
        }

        // Las join columns de Racha son insertable=false: se inserta con la query nativa
        rachaRepository.addRecompensaToCliente(
                clienteId,
                recompensaId,
                rachaDTO.getContadorDias() != null ? rachaDTO.getContadorDias() : 0,
                rachaDTO.isEntregada(),
                rachaDTO.getUltimaFechaRegistro() != null ? rachaDTO.getUltimaFechaRegistro() : LocalDateTime.now());

        return rachaMapper.toDto(getRachaOrThrow(clienteId, recompensaId));
    }

    @Transactional
    @Override
    public RachaDTO update(Integer clienteId, Integer recompensaId, RachaDTO updateRachaDTO) {
        Racha rachaFromDb = getRachaOrThrow(clienteId, recompensaId);

        if (updateRachaDTO.getContadorDias() != null) {
            rachaFromDb.setContadorDias(updateRachaDTO.getContadorDias());
        }
        rachaFromDb.setEntregada(updateRachaDTO.isEntregada());
        if (updateRachaDTO.getUltimaFechaRegistro() != null) {
            rachaFromDb.setUltimaFechaRegistro(updateRachaDTO.getUltimaFechaRegistro());
        }
        return rachaMapper.toDto(rachaRepository.save(rachaFromDb));
    }

    @Transactional
    @Override
    public void delete(Integer clienteId, Integer recompensaId) {
        getRachaOrThrow(clienteId, recompensaId);
        rachaRepository.deleteByClienteAndRecompensa(clienteId, recompensaId);
    }

    // Asignar una recompensa a un cliente = crear su racha con valores iniciales
    @Transactional
    @Override
    public RachaDTO asignarRachaACliente(Integer recompensaId, Integer clienteId) {
        RachaDTO nueva = new RachaDTO();
        nueva.setClienteId(clienteId);
        nueva.setRecompensaId(recompensaId);
        nueva.setContadorDias(0);
        nueva.setEntregada(false);
        return create(nueva);
    }

    private Racha getRachaOrThrow(Integer clienteId, Integer recompensaId) {
        return rachaRepository.findOneByClienteIdAndRecompensaId(clienteId, recompensaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Racha no encontrada para cliente " + clienteId + " y recompensa " + recompensaId));
    }
}
