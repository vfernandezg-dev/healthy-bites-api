package com.healthybites.service.impl;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.HabitoMapper;
import com.healthybites.model.entity.*;
import com.healthybites.repository.*;
import com.healthybites.service.HabitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
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

        for (Racha racha : rachas) {
            if (!racha.isEntregada()) {
                racha.setContadorDias(racha.getContadorDias() + 1);
                racha.setUltimaFechaRegistro(now);

                Recompensa recompensa = racha.getRecompensa();
                if (racha.getContadorDias() >= recompensa.getDiasRequeridos()) {
                    System.out.println("¡Felicidades! Has alcanzado una recompensa.");
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
