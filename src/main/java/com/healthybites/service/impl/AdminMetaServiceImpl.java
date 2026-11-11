package com.healthybites.service.impl;

import com.healthybites.dto.ClienteDetailsDTO;
import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ClienteMapper;
import com.healthybites.mapper.MetaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.model.enums.EstadoMeta;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.MetaRepository;
import com.healthybites.service.AdminMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminMetaServiceImpl implements AdminMetaService {

    private final MetaRepository metaRepository;
    private final MetaMapper metaMapper;
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    @Override
    public List<MetaDTO> getAll() {
        return metaRepository.findAll().stream()
                .map(metaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MetaDTO> paginate(Pageable pageable) {
        return metaRepository.findAll(pageable).map(metaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public MetaDTO findById(Integer id) {
        return metaMapper.toDTO(getMetaOrThrow(id));
    }

    @Transactional
    @Override
    public MetaDTO create(MetaCreateDTO metaCreateDTO) {
        Cliente cliente = getClienteOrThrow(metaCreateDTO.getClienteId());

        Meta meta = metaMapper.toEntity(metaCreateDTO);
        meta.setId(null);
        meta.setCliente(cliente);
        LocalDateTime ahora = LocalDateTime.now();
        meta.setFechaCreacion(ahora);
        meta.setFechaActualizacion(ahora);
        meta.setEstado(EstadoMeta.ACTIVE);
        meta.getHistorialEstados().add(EstadoMeta.ACTIVE);

        return metaMapper.toDTO(metaRepository.save(meta));
    }

    @Transactional
    @Override
    public MetaDTO update(Integer id, MetaCreateDTO updateMetaDTO) {
        Meta metaFromDB = getMetaOrThrow(id);

        metaFromDB.setNombre(updateMetaDTO.getNombre());
        metaFromDB.setDescripcion(updateMetaDTO.getDescripcion());
        metaFromDB.setPesoObjetivo(updateMetaDTO.getPesoObjetivo());
        if (updateMetaDTO.getClienteId() != null) {
            metaFromDB.setCliente(getClienteOrThrow(updateMetaDTO.getClienteId()));
        }
        metaFromDB.setFechaActualizacion(LocalDateTime.now());

        return metaMapper.toDTO(metaRepository.save(metaFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        metaRepository.delete(getMetaOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public String calcularRecomendacion(MetaCreateDTO metaCreateDTO) {
        Cliente cliente = getClienteOrThrow(metaCreateDTO.getClienteId());

        double diferenciaPeso = cliente.getPeso() - metaCreateDTO.getPesoObjetivo();

        if (diferenciaPeso > 5) {
            return "Se recomienda un plan de 6 meses para reducir gradualmente.";
        } else if (diferenciaPeso > 2) {
            return "Se recomienda un plan de 3 meses.";
        }
        return "Mantén tu peso actual con una dieta balanceada y ejercicio regular.";
    }

    @Transactional(readOnly = true)
    @Override
    public List<MetaDTO> findMetasByClienteId(Integer clienteId) {
        return metaRepository.findByClienteId(clienteId).stream()
                .map(metaMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public MetaDTO actualizarObjetivosSalud(Integer id, MetaCreateDTO metaCreateDTO) {
        return update(id, metaCreateDTO);
    }

    @Transactional
    @Override
    public MetaDTO actualizacionEstadoMeta(Integer id, EstadoMeta nuevoEstado) {
        if (nuevoEstado == null) {
            throw new BadRequestException("El nuevo estado es obligatorio (ACTIVE o COMPLETED)");
        }
        Meta metaFromDB = getMetaOrThrow(id);

        if (metaFromDB.getEstado() != nuevoEstado) {
            metaFromDB.setEstado(nuevoEstado);
            metaFromDB.getHistorialEstados().add(nuevoEstado);
            metaFromDB.setFechaActualizacion(LocalDateTime.now());
            metaFromDB = metaRepository.save(metaFromDB);
        }
        return metaMapper.toDTO(metaFromDB);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EstadoMeta> getHistorialById(Integer id) {
        // Copia dentro de la transaccion para no devolver una coleccion lazy
        return List.copyOf(getMetaOrThrow(id).getHistorialEstados());
    }

    // Completa la meta actual (si estaba activa) y crea una nueva meta activa con los mismos datos.
    // No esta expuesto en el controller; se conserva del codigo original.
    @Transactional
    public MetaDTO nuevoObjetivo(Integer id) {
        Meta metaFromDB = getMetaOrThrow(id);
        LocalDateTime ahora = LocalDateTime.now();

        if (metaFromDB.getEstado() == EstadoMeta.ACTIVE) {
            metaFromDB.setEstado(EstadoMeta.COMPLETED);
            metaFromDB.getHistorialEstados().add(EstadoMeta.COMPLETED);
            metaFromDB.setFechaActualizacion(ahora);
            metaRepository.save(metaFromDB);
        }

        Meta newMeta = new Meta();
        newMeta.setNombre(metaFromDB.getNombre());
        newMeta.setDescripcion(metaFromDB.getDescripcion());
        newMeta.setPesoObjetivo(metaFromDB.getPesoObjetivo());
        newMeta.setCliente(metaFromDB.getCliente());
        newMeta.setEstado(EstadoMeta.ACTIVE);
        newMeta.getHistorialEstados().add(EstadoMeta.ACTIVE);
        newMeta.setFechaCreacion(ahora);
        newMeta.setFechaActualizacion(ahora);

        return metaMapper.toDTO(metaRepository.save(newMeta));
    }

    @Transactional(readOnly = true)
    @Override
    public ClienteDetailsDTO getPerfilClienteByMetaId(Integer metaId) {
        return clienteMapper.toDTO(getMetaOrThrow(metaId).getCliente());
    }

    private Meta getMetaOrThrow(Integer id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La meta con ID " + id + " no fue encontrada"));
    }

    private Cliente getClienteOrThrow(Integer clienteId) {
        if (clienteId == null) {
            throw new BadRequestException("El ID del cliente es obligatorio");
        }
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("El cliente con ID " + clienteId + " no fue encontrado"));
    }
}
