package com.healthybites.service.impl;

import com.healthybites.dto.RecompensaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.RecompensaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Racha;
import com.healthybites.model.entity.Recompensa;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.RachaRepository;
import com.healthybites.repository.RecompensaRepository;
import com.healthybites.service.AdminRecompensaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class AdminRecompensaServiceImpl implements AdminRecompensaService {


    @Autowired
    private final RecompensaRepository recompensaRepository;

    @Autowired
    private final RecompensaMapper recompensaMapper;

    @Autowired
    private final ClienteRepository clienteRepository; // Añadir el repositorio de clientes

    @Transactional(readOnly = true)
    @Override
    public List<RecompensaDTO> getAll() {
        List<Recompensa> recompensas = recompensaRepository.findAll();
        return recompensas.stream()
                .map(recompensaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RecompensaDTO> paginate(Pageable pageable) {
        Page<Recompensa> recompensas = recompensaRepository.findAll(pageable);
        return recompensas.map(recompensaMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RecompensaDTO findById(Integer id) {
        Recompensa recompensa = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La recompensa no fue encontrada"));
        return recompensaMapper.toDto(recompensa);
    }

    @Transactional
    @Override
    public RecompensaDTO create(RecompensaDTO recompensaDTO) {
        Recompensa recompensa = recompensaMapper.toEntity(recompensaDTO);
        recompensaRepository.save(recompensa);
        return recompensaMapper.toDto(recompensa);
    }

    @Transactional
    @Override
    public RecompensaDTO update(Integer id, RecompensaDTO updateRecompensaDTO) {
        Recompensa recompensaFromDb = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La recompensa no existe"));

        recompensaFromDb.setNombre(updateRecompensaDTO.getNombre());
        recompensaFromDb.setDescripcion(updateRecompensaDTO.getDescripcion());
        recompensaFromDb.setDiasRequeridos(updateRecompensaDTO.getDiasRequeridos());

        recompensaFromDb = recompensaRepository.save(recompensaFromDb);
        return recompensaMapper.toDto(recompensaFromDb);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Recompensa recompensa = recompensaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La recompensa no existe"));
        recompensaRepository.delete(recompensa);
    }



    @Transactional
    @Override
    public RecompensaDTO asignarRecompensaACliente(Integer recompensaId, Integer clienteId) {
        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada: " + recompensaId));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));

        // Asumiendo que tienes una relación entre Recompensa y Cliente
        recompensa.setCliente(cliente); // Esto asigna el cliente a la recompensa.
        recompensaRepository.save(recompensa); // Guarda la recompensa actualizada.

        return recompensaMapper.toDto(recompensa); // Retorna la recompensa actualizada como DTO.
    }


}
