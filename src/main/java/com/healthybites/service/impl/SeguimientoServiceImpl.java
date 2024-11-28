package com.healthybites.service.impl;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.SeguimientoMapper;
import com.healthybites.model.entity.Meta;
import com.healthybites.model.entity.Seguimiento;
import com.healthybites.repository.MetaRepository;
import com.healthybites.repository.SeguimientoRepository;
import com.healthybites.service.SeguimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SeguimientoServiceImpl implements SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final MetaRepository metaRepository;
    private final SeguimientoMapper seguimientoMapper;

    @Override
    public List<SeguimientoDTO> getAll(Integer metaId) {
        List<Seguimiento> seguimientos = seguimientoRepository.findByMetaId(metaId);

        return seguimientos.stream()
                .map(seguimientoMapper::toDTO)
                .toList();
    }

    @Override
    public List<SeguimientoDTO> getAllSeguimientosByMetaIdAndClienteId(Integer metaId, Integer clienteId) {
        List<Seguimiento> seguimientos = seguimientoRepository.findByMetaIdAndMetaClienteId(metaId, clienteId);
        return seguimientos.stream()
                .map(seguimientoMapper::toDTO)
                .toList();
    }

    @Override
    public Page<SeguimientoDTO> paginateSeguimientosByMetaIdAndClienteId(Integer metaId, Integer clienteId, Pageable pageable) {
        return seguimientoRepository.findByMetaIdAndMetaClienteId(metaId, clienteId, pageable)
                .map(seguimientoMapper::toDTO);
    }

    @Override
    public Page<SeguimientoDTO> paginate(Pageable pageable) {
        return seguimientoRepository.findAll(pageable)
                .map(seguimientoMapper::toDTO);
    }

    @Override
    public SeguimientoDTO findByIdAndMetaId(Integer seguimientoId, Integer metaId) {
        Seguimiento seguimiento = seguimientoRepository.findByIdAndMetaId(seguimientoId, metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento con id " + seguimientoId + " y meta con id " + metaId + " no encontrado"));
        return seguimientoMapper.toDTO(seguimiento);
    }

    @Override
    public SeguimientoDTO create(Integer metaId, SeguimientoCreateDTO seguimientoCreateDTO) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " no encontrado"));

        Seguimiento seguimiento = seguimientoMapper.toEntity(seguimientoCreateDTO);

        seguimiento.setMeta(meta);

        seguimiento.setFechaCreacion(LocalDateTime.now());
        seguimiento.setFechaActualizacion(LocalDateTime.now());

        return seguimientoMapper.toDTO(seguimientoRepository.save(seguimiento));
    }

    @Override
    public SeguimientoDTO update(Integer metaId, Integer seguimientoId, SeguimientoCreateDTO updatedSeguimientoDTO) {
        Seguimiento seguimiento = seguimientoRepository.findByIdAndMetaId(seguimientoId, metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento con id " + seguimientoId + " y meta con id " + metaId + " no encontrado"));

        // Asegúrate de que el seguimiento pertenece a la meta
        if (!seguimiento.getMeta().getId().equals(metaId)) {
            throw new IllegalArgumentException("El seguimiento no pertenece a la meta especificada");
        }

        // Actualiza los campos del seguimiento
        seguimientoMapper.updateFromDTO(updatedSeguimientoDTO, seguimiento);

        // Convertir la fecha del string a LocalDate y obtener la hora actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(updatedSeguimientoDTO.getFecha(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use 'dd-MM-yyyy'.", e);
        }
        LocalTime horaActual = LocalTime.now();

        // Combinar la fecha con la hora actual del sistema
        seguimiento.setFecha(LocalDateTime.of(fecha, horaActual));
        seguimiento.setFechaActualizacion(LocalDateTime.now());

        // Guarda los cambios
        return seguimientoMapper.toDTO(seguimientoRepository.save(seguimiento));
    }

    @Override
    public void delete(Integer seguimientoId, Integer metaId) {
        Seguimiento seguimiento = seguimientoRepository.findByIdAndMetaId(seguimientoId, metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento con id " + seguimientoId + " y meta con id " + metaId + " no encontrado"));
        seguimientoRepository.delete(seguimiento);
    }
}
