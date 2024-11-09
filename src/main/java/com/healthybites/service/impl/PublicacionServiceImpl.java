package com.healthybites.service.impl;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.PublicacionMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Grupo;
import com.healthybites.model.entity.Publicacion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.repository.PublicacionRepository;
import com.healthybites.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PublicacionServiceImpl implements PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final ClienteRepository clienteRepository;
    private final GrupoRepository grupoRepository;
    private final PublicacionMapper publicacionMapper;

    @Override
    public List<PublicacionDTO> getAll(Integer id) {
        List<Publicacion> publicaciones = publicacionRepository.findByClienteId(id);

        return publicaciones.stream()
                .map(publicacionMapper::toDTO)
                .toList();
    }

    @Override
    public PublicacionDTO findByIdAndClienteId(Integer publicacionId, Integer clienteId) {
        Publicacion publicacion = publicacionRepository.findByIdAndClienteId(publicacionId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion con id " + publicacionId + " y clienteId " + clienteId + " no encontrado"));
        return publicacionMapper.toDTO(publicacion);
    }

    @Override
    public PublicacionDTO create(PublicacionCreateDTO publicacionCreateDTO) {
        Cliente cliente = clienteRepository.findById(publicacionCreateDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + publicacionCreateDTO.getClienteId() + " no encontrado"));

        Grupo grupo = grupoRepository.findById(publicacionCreateDTO.getGrupoId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo con id " + publicacionCreateDTO.getGrupoId() + " no encontrado"));

        Publicacion publicacion = publicacionMapper.toEntity(publicacionCreateDTO);

        publicacion.setCliente(cliente);
        publicacion.setGrupo(grupo);
        publicacion.setFechaCreacion(LocalDateTime.now());
        publicacion.setFechaActualizacion(LocalDateTime.now());

        Publicacion savedPublicacion = publicacionRepository.save(publicacion);

        return publicacionMapper.toDTO(savedPublicacion);
    }

    @Override
    public PublicacionDTO update(Integer publicacionId, Integer clienteId, PublicacionCreateDTO updatedPublicacionDTO) {
        Publicacion publicacion = publicacionRepository.findByIdAndClienteId(publicacionId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion con id " + publicacionId + " y clienteId " + clienteId + " no encontrado"));

        publicacionMapper.updateFromDTO(updatedPublicacionDTO, publicacion);

        publicacion.setFechaActualizacion(LocalDateTime.now());

        return publicacionMapper.toDTO(publicacionRepository.save(publicacion));
    }

    @Override
    public void delete(Integer publicacionId, Integer clienteId) {
        Publicacion publicacion = publicacionRepository.findByIdAndClienteId(publicacionId, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion con id " + publicacionId + " y clienteId " + clienteId + " no encontrado"));

        publicacionRepository.delete(publicacion);
    }
}
