package com.healthybites.service.impl;

import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.mapper.RecompensaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Recompensa;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.RachaRepository;
import com.healthybites.repository.RecompensaRepository;
import com.healthybites.service.RachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RachaServiceImpl implements RachaService {

    private final RachaRepository rachaRepository;
    private final RecompensaRepository recompensaRepository;
    private final ClienteRepository clienteRepository;
    private final RecompensaMapper recompensaMapper;

    @Override
    public RecompensaDetailsDTO addRecompensaToClient(Integer clientId, Integer recompensaId) {
        LocalDateTime fecha = LocalDateTime.now();

        // Verificar si la recompensa ya está asignada al cliente
        boolean recompensaAsignada = rachaRepository.existsByClienteIdAndRecompensaId(clientId, recompensaId);
        if (recompensaAsignada) {
            throw new IllegalArgumentException("La recompensa ya está asignada al cliente");
        }

        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new RuntimeException("Recompensa no encontrada"));

        rachaRepository.addRecompensaToCliente(clientId, recompensaId, 0, false, fecha);

        return recompensaMapper.toDTO(recompensa);
    }

    @Override
    public void removeRecompensaFromClient(Integer clientId, Integer recompensaId) {
        boolean recompensaPertenece = rachaRepository.existsByClienteIdAndRecompensaId(clientId, recompensaId);

        if (!recompensaPertenece) {
            throw new IllegalArgumentException("La recompensa no pertenece al cliente");
        }

        rachaRepository.deleteByClienteAndRecompensa(clientId, recompensaId);
    }

    @Override
    public List<RecompensaDetailsDTO> getAllRecompensasByClient(Integer clientId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<Recompensa> recompensas = rachaRepository.findRachaByCliente(cliente);

        return recompensas.stream()
                .map(recompensaMapper::toDTO)
                .collect(Collectors.toList());
    }
}
