package com.healthybites.service.impl;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.SeguimientoMapper;
import com.healthybites.model.entity.Seguimiento;
import com.healthybites.repository.MetaRepository;
import com.healthybites.repository.SeguimientoRepository;
import com.healthybites.service.AdminSeguimientoService;
import com.healthybites.service.SeguimientoService;
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
public class AdminSeguimientoServiceImpl implements AdminSeguimientoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final SeguimientoRepository seguimientoRepository;
    private final MetaRepository metaRepository;
    private final SeguimientoMapper seguimientoMapper;
    private final SeguimientoService seguimientoService;

    @Transactional(readOnly = true)
    @Override
    public List<SeguimientoDTO> getAll() {
        return seguimientoRepository.findAll().stream()
                .map(seguimientoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SeguimientoDTO> paginate(Pageable pageable) {
        return seguimientoRepository.findAll(pageable).map(seguimientoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public SeguimientoDTO findById(Integer id) {
        return seguimientoMapper.toDTO(getSeguimientoOrThrow(id));
    }

    @Transactional
    @Override
    public SeguimientoDTO create(Integer metaId, SeguimientoCreateDTO seguimientoCreateDTO) {
        if (seguimientoCreateDTO.getFecha() == null || seguimientoCreateDTO.getFecha().isBlank()) {
            seguimientoCreateDTO.setFecha(LocalDate.now().format(FORMATO_FECHA));
        }
        try {
            // Reutiliza la logica del flujo normal (valida la meta y asigna fechas)
            return seguimientoService.create(metaId, seguimientoCreateDTO);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("La fecha debe tener el formato dd-MM-yyyy");
        }
    }

    @Transactional
    @Override
    public SeguimientoDTO update(Integer id, Integer metaId, SeguimientoCreateDTO updateSeguimiento) {
        Seguimiento seguimientoFromDB = getSeguimientoOrThrow(id);

        seguimientoFromDB.setPesoDelDia(updateSeguimiento.getPesoDelDia());
        seguimientoFromDB.setObservaciones(updateSeguimiento.getObservaciones());
        if (updateSeguimiento.getFecha() != null && !updateSeguimiento.getFecha().isBlank()) {
            try {
                LocalDate fecha = LocalDate.parse(updateSeguimiento.getFecha(), FORMATO_FECHA);
                seguimientoFromDB.setFecha(LocalDateTime.of(fecha, LocalTime.now()));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("La fecha debe tener el formato dd-MM-yyyy");
            }
        }
        if (metaId != null) {
            seguimientoFromDB.setMeta(metaRepository.findById(metaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meta no encontrada por ID: " + metaId)));
        }
        seguimientoFromDB.setFechaActualizacion(LocalDateTime.now());

        return seguimientoMapper.toDTO(seguimientoRepository.save(seguimientoFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        seguimientoRepository.delete(getSeguimientoOrThrow(id));
    }

    private Seguimiento getSeguimientoOrThrow(Integer id) {
        return seguimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento no encontrado por ID: " + id));
    }
}
