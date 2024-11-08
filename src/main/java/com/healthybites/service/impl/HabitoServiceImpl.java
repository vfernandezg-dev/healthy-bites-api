package com.healthybites.service.impl;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.HabitoMapper;
import com.healthybites.model.entity.*;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.HabitoRepository;
import com.healthybites.repository.RachaRepository;
import com.healthybites.repository.RecompensaRepository;
import com.healthybites.service.HabitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class HabitoServiceImpl implements HabitoService {

    private final HabitoRepository habitoRepository;
    private final RachaRepository rachaRepository;
    private final ClienteRepository clienteRepository;
    private final RecompensaRepository recompensaRepository;
    private final HabitoMapper habitoMapper;

    @Override
    public List<HabitoDTO> getAll() {
        List<Habito> habitos = habitoRepository.findAll();
        return habitos.stream()
                .map(habitoMapper::toDTO)
                .toList();
    }

    @Override
    public HabitoDTO registrarHabito(HabitoCreateDTO habitoCreateDTO) {
        LocalDateTime now = LocalDateTime.now();
        Integer clienteId = habitoCreateDTO.getClienteId();

        // Buscar cliente y lanzar excepción si no existe
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clienteId));

        // Crear y guardar el nuevo hábito
        Habito habito = habitoMapper.toEntity(habitoCreateDTO);
        habito.setFechaRegistro(now);
        habito.setFechaActualizacion(now);
        habito.setCliente(cliente);
        Habito nuevoHabito = habitoRepository.save(habito);

        // Incrementar el contador de días de todas las rachas del cliente
        incrementarContadorRachas(cliente.getId(), now);

        return habitoMapper.toDTO(nuevoHabito);
    }

    @Override
    public HabitoDTO actualizarHabito(Integer habitoId, HabitoCreateDTO habitoCreateDTO) {
        Habito habito = habitoRepository.findById(habitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado"));
        habitoMapper.updateHabitoFromDto(habitoCreateDTO, habito);
        Habito habitoActualizado = habitoRepository.save(habito);
        return habitoMapper.toDTO(habitoActualizado);
    }

    @Override
    public void delete(Integer habitoId) {
        Habito habito = habitoRepository.findById(habitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado con id: " + habitoId));
        habitoRepository.delete(habito);
    }

    private void incrementarContadorRachas(Integer clienteId, LocalDateTime now) {
        // Obtiene todas las rachas del cliente
        List<Racha> rachas = rachaRepository.findByCliente(clienteId);

        for (Racha racha : rachas) {
            // Solo incrementar si la recompensa no ha sido entregada aún
            if (!racha.isEntregada()) {
                // Incrementa el contador de días
                racha.setContadorDias(racha.getContadorDias() + 1);
                racha.setUltimaFechaRegistro(now);

                // Verificar si el cliente alcanzó la recompensa
                Recompensa recompensa = racha.getRecompensa();
                if (racha.getContadorDias() >= recompensa.getDiasRequeridos()) {
                    // Mensaje de recompensa alcanzada
                    System.out.println("¡Felicidades! Has alcanzado la recompensa: " + recompensa.getNombre());

                    // Marca la recompensa como entregada para este cliente
                    racha.setEntregada(true);
                }

                // Guarda los cambios en la base de datos
                rachaRepository.save(racha);
            }
        }
    }




}
