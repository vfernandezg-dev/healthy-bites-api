package com.healthybites.service.impl;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.HabitoMapper;
import com.healthybites.model.entity.*;
import com.healthybites.repository.*;
import com.healthybites.service.HabitoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class HabitoServiceImpl implements HabitoService {

    private final HabitoRepository habitoRepository;
    private final RachaRepository rachaRepository;
    private final ClienteRepository clienteRepository;
    private final AccesoContenidoRepository accesoContenidoRepository;
    private final AccesoPlanRepository accesoPlanRepository;
    private final HabitoMapper habitoMapper;

    @Override
    public List<HabitoDTO> getAll(Integer id) {
        List<Habito> habitos = habitoRepository.findByClienteId(id);
        return habitos.stream()
                .map(habitoMapper::toDTO)
                .toList();
    }

    @Override
    public HabitoDTO findById(Integer id) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado con id: " + id));
        return habitoMapper.toDTO(habito);
    }

    @Override
    @Transactional
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
        List<Racha> rachas = rachaRepository.findByCliente(clienteId);

        LocalDate today = now.toLocalDate();
        for (Racha racha : rachas) {
            if (!racha.isEntregada()) {
                // Como mucho un día por fecha (antes cada hábito registrado sumaba un día y se podían desbloquear
                // recompensas en segundos); un día sin registro reinicia la racha.
                LocalDate last = racha.getUltimaFechaRegistro() == null ? null : racha.getUltimaFechaRegistro().toLocalDate();
                boolean started = racha.getContadorDias() > 0;
                if (started && today.equals(last)) {
                    continue;
                }
                boolean consecutive = started && last != null && last.plusDays(1).equals(today);
                racha.setContadorDias(consecutive ? racha.getContadorDias() + 1 : 1);
                racha.setUltimaFechaRegistro(now);

                Recompensa recompensa = racha.getRecompensa();
                if (racha.getContadorDias() >= recompensa.getDiasRequeridos()) {
                    log.info("Recompensa {} conseguida por el cliente {}", recompensa.getId(), clienteId);
                    racha.setEntregada(true);

                    // Verifica el tipo de recompensa
                    if (recompensa.getContenido() != null) {
                        otorgarAccesoAContenido(clienteId, recompensa.getContenido());
                    } else if (recompensa.getPlanAlimenticio() != null) {
                        otorgarAccesoAPlan(clienteId, recompensa.getPlanAlimenticio());
                    }
                }

                rachaRepository.save(racha);
            }
        }
    }

    private void otorgarAccesoAContenido(Integer clienteId, Contenido contenido) {
        LocalDateTime now = LocalDateTime.now();
        accesoContenidoRepository.addContenidoToCliente(clienteId, contenido.getId(), now);
    }

    private void otorgarAccesoAPlan(Integer clienteId, PlanAlimenticio plan) {
        LocalDateTime now = LocalDateTime.now();
        accesoPlanRepository.addPlanToCliente(clienteId, plan.getId(), now);
    }

}
