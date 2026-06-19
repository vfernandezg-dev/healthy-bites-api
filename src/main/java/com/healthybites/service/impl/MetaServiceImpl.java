package com.healthybites.service.impl;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.dto.SeguimientoCreateUpdateDTO;
import com.healthybites.dto.SeguimientoDetailsDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.MetaMapper;
import com.healthybites.mapper.SeguimientoMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.model.entity.Seguimiento;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.MetaRepository;
import com.healthybites.repository.SeguimientoRepository;
import com.healthybites.service.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MetaServiceImpl implements MetaService {

    private final MetaRepository metaRepository;
    private final ClienteRepository clienteRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final MetaMapper metaMapper;
    private final SeguimientoMapper seguimientoMapper;

    @Override
    public List<MetaDTO> getAll() {
        List<Meta> metas = metaRepository.findAll();
        return metas.stream()
                .map(metaMapper::toDTO)
                .toList();
    }

    @Override
    public List<SeguimientoDetailsDTO> getAllSeguimientosByMetaId(Integer metaId) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " no encontrada"));

        // Recuperar todos los seguimientos asociados a la meta
        List<Seguimiento> seguimientos = seguimientoRepository.findByMetaId(metaId);

        // Mapear a DTOs
        return seguimientos.stream()
                .map(seguimientoMapper::toDTO)
                .toList();
    }


    @Override
    public MetaDTO findById(Integer id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + id + " no encontrada"));
        return metaMapper.toDTO(meta);
    }

    @Override
    public MetaDTO create(MetaCreateDTO metaCreateDTO) {
        Cliente cliente = clienteRepository.findById(metaCreateDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + metaCreateDTO.getClienteId() + " no encontrado"));

        Meta meta = metaMapper.toEntity(metaCreateDTO);
        meta.setCliente(cliente);
        meta.setFechaCreacion(LocalDateTime.now());
        meta.setFechaActualizacion(LocalDateTime.now());

        return metaMapper.toDTO(metaRepository.save(meta));
    }

    @Override
    public SeguimientoDetailsDTO addSeguimientoToMeta(Integer metaId, SeguimientoCreateUpdateDTO seguimientoDTO) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " no encontrada"));

        Seguimiento seguimiento = seguimientoMapper.toEntity(seguimientoDTO);
        seguimiento.setMeta(meta);  // Asociar seguimiento con la meta
        seguimiento.setFechaCreacion(LocalDateTime.now());
        seguimiento.setFechaActualizacion(LocalDateTime.now());

        Seguimiento savedSeguimiento = seguimientoRepository.save(seguimiento);

        return seguimientoMapper.toDTO(savedSeguimiento);
    }

    @Override
    public SeguimientoDetailsDTO getSeguimientoById(Integer metaId, Integer seguimientoId) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " no encontrada"));

        Seguimiento seguimiento = seguimientoRepository.findById(seguimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento con id " + seguimientoId + " no encontrado"));

        // Verificar que el seguimiento esté asociado a la meta correcta
        if (!seguimiento.getMeta().getId().equals(metaId)) {
            throw new ResourceNotFoundException("El seguimiento no está asociado a la meta con id " + metaId);
        }

        return seguimientoMapper.toDTO(seguimiento);
    }


    @Override
    public MetaDTO update(Integer id, MetaCreateDTO updatedMetaDTO) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + id + " no encontrada"));

        Cliente cliente = clienteRepository.findById(updatedMetaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + updatedMetaDTO.getClienteId() + " no encontrado"));

        meta.setCliente(cliente);
        meta.setNombre(updatedMetaDTO.getNombre());
        meta.setDescripcion(updatedMetaDTO.getDescripcion());
        meta.setPesoObjetivo(updatedMetaDTO.getPesoObjetivo());

        meta.setFechaActualizacion(LocalDateTime.now());

        return metaMapper.toDTO(metaRepository.save(meta));
    }

    @Override
    public SeguimientoDetailsDTO updateSeguimiento(Integer metaId, Integer seguimientoId, SeguimientoCreateUpdateDTO seguimientoUpdateDTO) {
        // Verificar si la meta existe
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " no encontrada"));

        // Verificar si el seguimiento existe
        Seguimiento seguimiento = seguimientoRepository.findById(seguimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento con id " + seguimientoId + " no encontrado"));

        // Asegúrate de que el seguimiento pertenece a la meta
        if (!seguimiento.getMeta().getId().equals(metaId)) {
            throw new IllegalArgumentException("El seguimiento no pertenece a la meta especificada");
        }

        // Actualiza los campos del seguimiento
        seguimientoMapper.toEntity(seguimientoUpdateDTO);

        // Convertir la fecha del string a LocalDate y obtener la hora actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(seguimientoUpdateDTO.getFecha(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use 'dd-MM-yyyy'.", e);
        }
        LocalTime horaActual = LocalTime.now();

        // Combinar la fecha con la hora actual del sistema
        seguimiento.setFecha(LocalDateTime.of(fecha, horaActual));
        seguimiento.setPesoDelDia(seguimientoUpdateDTO.getPesoDelDia());
        seguimiento.setObservaciones(seguimientoUpdateDTO.getObservaciones());
        seguimiento.setFechaActualizacion(LocalDateTime.now());

        // Guarda los cambios
        return seguimientoMapper.toDTO(seguimientoRepository.save(seguimiento));
    }

    @Override
    public void delete(Integer id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + id + " no encontrada"));
        metaRepository.delete(meta);
    }

    @Override
    public void deleteSeguimiento(Integer metaId, Integer seguimientoId) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " no encontrada"));

        Seguimiento seguimiento = seguimientoRepository.findById(seguimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento con id " + seguimientoId + " no encontrado"));

        if (!seguimiento.getMeta().getId().equals(metaId)) {
            throw new IllegalArgumentException("El seguimiento no pertenece a la meta especificada");
        }

        seguimientoRepository.delete(seguimiento); // Elimina el seguimiento específico
    }


}
