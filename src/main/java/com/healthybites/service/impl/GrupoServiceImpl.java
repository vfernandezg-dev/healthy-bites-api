package com.healthybites.service.impl;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.GrupoMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Grupo;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GrupoServiceImpl implements GrupoService {
    private final GrupoRepository grupoRepository;
    private final ClienteRepository clienteRepository;
    private final GrupoMapper grupoMapper;

    @Override
    public List<GrupoDTO> getAll(Integer id) {
        List<Grupo> grupos = grupoRepository.findByClienteId(id);

        return grupos.stream()
                .map(grupoMapper::toDTO)
                .toList();
    }

    @Override
    public GrupoDTO findByIdAndClienteId(Integer grupoId, Integer clienteId) {
        Grupo grupo = grupoRepository.findByIdAndClienteId(grupoId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo con id " + grupoId + " y clienteId " + clienteId + " no encontrado"));
        return grupoMapper.toDTO(grupo);
    }

    @Override
    public GrupoDTO create(GrupoCreateDTO grupoCreateDTO) {
        Cliente cliente = clienteRepository.findById(grupoCreateDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + grupoCreateDTO.getClienteId() + " no encontrado"));

        Grupo grupo = grupoMapper.toEntity(grupoCreateDTO);

        grupo.setCliente(cliente);
        grupo.setEsPrivado(grupoCreateDTO.isEsPrivado());
        grupo.setCantidadMiembros(1);

        grupo.setFechaCreacion(LocalDateTime.now());
        grupo.setFechaActualizacion(LocalDateTime.now());

        return grupoMapper.toDTO(grupoRepository.save(grupo));
    }

    @Override
    public GrupoDTO update(Integer grupoId, Integer clienteId, GrupoCreateDTO updatedGrupoDTO) {
        Grupo grupo = grupoRepository.findByIdAndClienteId(grupoId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo con id " + grupoId + " y clienteId " + clienteId + " no encontrado"));

        Cliente cliente = clienteRepository.findById(updatedGrupoDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + updatedGrupoDTO.getClienteId() + " no encontrado"));

        grupo.setCliente(cliente);
        grupo.setEsPrivado(updatedGrupoDTO.isEsPrivado());

        grupo.setFechaActualizacion(LocalDateTime.now());

        return grupoMapper.toDTO(grupoRepository.save(grupo));
    }

    @Override
    public void delete(Integer grupoId, Integer clienteId) {
        Grupo grupo = grupoRepository.findByIdAndClienteId(grupoId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo con id " + grupoId + " y clienteId " + clienteId + " no encontrado"));
        grupoRepository.delete(grupo);
    }
}
