package com.healthybites.service.impl;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.HabitoMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Habito;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.HabitoRepository;
import com.healthybites.service.AdminHabitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminHabitoServiceImpl implements AdminHabitoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final HabitoRepository habitoRepository;
    private final ClienteRepository clienteRepository;
    private final HabitoMapper habitoMapper;

    @Transactional(readOnly = true)
    @Override
    public List<HabitoDTO> getAll() {
        return habitoRepository.findAll().stream()
                .map(habitoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<HabitoDTO> paginate(Pageable pageable) {
        return habitoRepository.findAll(pageable).map(habitoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public HabitoDTO findById(Integer id) {
        return habitoMapper.toDTO(getHabitoOrThrow(id));
    }

    // Creacion administrativa: a diferencia de HabitoService.registrarHabito no actualiza rachas.
    @Transactional
    @Override
    public HabitoDTO create(HabitoCreateDTO habitoCreateDTO) {
        Habito habito;
        try {
            habito = habitoMapper.toEntity(habitoCreateDTO);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La fecha de registro debe tener el formato dd-MM-yyyy");
        }
        habito.setId(null);
        habito.setCliente(habitoCreateDTO.getClienteId() != null
                ? getClienteOrThrow(habitoCreateDTO.getClienteId())
                : null);
        habito.setFechaActualizacion(LocalDateTime.now());

        return habitoMapper.toDTO(habitoRepository.save(habito));
    }

    @Transactional
    @Override
    public HabitoDTO update(Integer id, HabitoCreateDTO updateHabitoDTO) {
        Habito habitoFromDB = getHabitoOrThrow(id);

        habitoFromDB.setNombre(updateHabitoDTO.getNombre());
        habitoFromDB.setHidratacion(updateHabitoDTO.getHidratacion());
        habitoFromDB.setAlimentacion(updateHabitoDTO.getAlimentacion());
        habitoFromDB.setEjercicio(updateHabitoDTO.getEjercicio());
        habitoFromDB.setCalidadDeSueno(updateHabitoDTO.getCalidadDeSueno());
        if (updateHabitoDTO.getFechaRegistro() != null) {
            try {
                LocalDate fecha = LocalDate.parse(updateHabitoDTO.getFechaRegistro(), FORMATO_FECHA);
                habitoFromDB.setFechaRegistro(LocalDateTime.of(fecha, LocalTime.now()));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("La fecha de registro debe tener el formato dd-MM-yyyy");
            }
        }
        if (updateHabitoDTO.getClienteId() != null) {
            habitoFromDB.setCliente(getClienteOrThrow(updateHabitoDTO.getClienteId()));
        }
        habitoFromDB.setFechaActualizacion(LocalDateTime.now());

        return habitoMapper.toDTO(habitoRepository.save(habitoFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        habitoRepository.delete(getHabitoOrThrow(id));
    }

    @Transactional
    @Override
    public HabitoDTO asignarHabitoACliente(Integer habitoId, Integer clienteId) {
        Habito habito = getHabitoOrThrow(habitoId);
        habito.setCliente(getClienteOrThrow(clienteId));
        habito.setFechaActualizacion(LocalDateTime.now());
        return habitoMapper.toDTO(habitoRepository.save(habito));
    }

    private Habito getHabitoOrThrow(Integer id) {
        return habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
    }

    private Cliente getClienteOrThrow(Integer clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));
    }
}
