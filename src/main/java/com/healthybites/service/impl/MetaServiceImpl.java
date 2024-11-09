package com.healthybites.service.impl;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.MetaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.MetaRepository;
import com.healthybites.service.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MetaServiceImpl implements MetaService {

    private final MetaRepository metaRepository;
    private final ClienteRepository clienteRepository;
    private final MetaMapper metaMapper;

    @Override
    public List<MetaDTO> getAll(Integer id) {
        List<Meta> metas = metaRepository.findByClienteId(id);

        return metas.stream()
                .map(metaMapper::toDTO)
                .toList();
    }

    @Override
    public MetaDTO findByIdAndClienteId(Integer metaId, Integer clienteId) {
        Meta meta = metaRepository.findByIdAndClienteId(metaId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " y cliente con id " + clienteId + " no encontrada"));
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
    public MetaDTO update(Integer metaId, Integer clienteId, MetaCreateDTO updatedMetaDTO) {
        Meta meta = metaRepository.findByIdAndClienteId(metaId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " y cliente con id " + clienteId + " no encontrada"));

        metaMapper.updateFromDTO(updatedMetaDTO, meta);

        meta.setFechaActualizacion(LocalDateTime.now());

        return metaMapper.toDTO(metaRepository.save(meta));
    }

    @Override
    public void delete(Integer metaId, Integer clienteId) {
        Meta meta = metaRepository.findByIdAndClienteId(metaId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta con id " + metaId + " y cliente con id " + clienteId + " no encontrada"));
        metaRepository.delete(meta);
    }
}
