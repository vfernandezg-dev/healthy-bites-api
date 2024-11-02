package com.healthybites.service.impl;

import com.healthybites.dto.ComentarioCreateDTO;
import com.healthybites.dto.ComentarioDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ComentarioMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Comentario;
import com.healthybites.model.entity.Publicacion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.ComentarioRepository;
import com.healthybites.repository.PublicacionRepository;
import com.healthybites.service.ComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ComentarioServiceImpl implements ComentarioService {
    private final ComentarioRepository comentarioRepository;
    private final ClienteRepository clienteRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioMapper comentarioMapper;

    @Override
    public List<ComentarioDTO> getAll(Integer publicacionId) {
        List<Comentario> comentarios = comentarioRepository.findByPublicacionId(publicacionId);

        return comentarios.stream()
                .map(comentarioMapper::toDTO)
                .toList();
    }

    @Override
    public ComentarioDTO findByIdAndPublicacionId(Integer comentarioId, Integer publicacionId) {
        Comentario comentario = comentarioRepository.findByIdAndPublicacionId(comentarioId, publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario con id " + comentarioId + " y publicacionId " + publicacionId + " no encontrado"));

        return comentarioMapper.toDTO(comentario);
    }

    @Override
    public ComentarioDTO create(Integer publicacionId, ComentarioCreateDTO comentarioCreateDTO) {
        Cliente cliente = clienteRepository.findById(comentarioCreateDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + comentarioCreateDTO.getClienteId() + " no encontrado"));

        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion con id " + publicacionId + " no encontrado"));

        Comentario comentario = comentarioMapper.toEntity(comentarioCreateDTO);

        comentario.setMensaje(comentarioCreateDTO.getMensaje());
        comentario.setCliente(cliente);
        comentario.setPublicacion(publicacion);

        comentario.setFechaCreacion(LocalDateTime.now());
        comentario.setFechaActualizacion(LocalDateTime.now());


        return comentarioMapper.toDTO(comentarioRepository.save(comentario));
    }

    @Override
    public ComentarioDTO update(Integer comentarioId, Integer publicacionId, ComentarioCreateDTO updatedComentarioDTO) {
        Comentario comentario = comentarioRepository.findByIdAndPublicacionId(comentarioId, publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario con id " + comentarioId + " y publicacionId " + publicacionId + " no encontrado"));

        comentario.setMensaje(updatedComentarioDTO.getMensaje());
        comentario.setFechaActualizacion(LocalDateTime.now());

        return comentarioMapper.toDTO(comentarioRepository.save(comentario));
    }

    @Override
    public void delete(Integer comentarioId, Integer publicacionId) {
        Comentario comentario = comentarioRepository.findByIdAndPublicacionId(comentarioId, publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario con id " + comentarioId + " y publicacionId " + publicacionId + " no encontrado"));

        comentarioRepository.delete(comentario);
    }
}
