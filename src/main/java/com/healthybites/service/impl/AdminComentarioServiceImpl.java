package com.healthybites.service.impl;

import com.healthybites.dto.ComentarioCreateDTO;
import com.healthybites.dto.ComentarioDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ComentarioMapper;
import com.healthybites.model.entity.Comentario;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.ComentarioRepository;
import com.healthybites.repository.PublicacionRepository;
import com.healthybites.service.AdminComentarioService;
import com.healthybites.service.ComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminComentarioServiceImpl implements AdminComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ClienteRepository clienteRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioMapper comentarioMapper;
    private final ComentarioService comentarioService;

    @Transactional(readOnly = true)
    @Override
    public List<ComentarioDTO> getAll() {
        return comentarioRepository.findAll().stream()
                .map(comentarioMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ComentarioDTO> paginate(Pageable pageable) {
        return comentarioRepository.findAll(pageable).map(comentarioMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public ComentarioDTO findById(Integer id) {
        return comentarioMapper.toDTO(getComentarioOrThrow(id));
    }

    @Transactional
    @Override
    public ComentarioDTO create(Integer publicacionId, ComentarioCreateDTO comentarioCreateDTO) {
        if (comentarioCreateDTO.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente es obligatorio");
        }
        // Reutiliza la logica del flujo normal (valida cliente/publicacion y asigna fechas)
        return comentarioService.create(publicacionId, comentarioCreateDTO);
    }

    @Transactional
    @Override
    public ComentarioDTO update(Integer id, Integer publicacionId, ComentarioCreateDTO updateComentario) {
        Comentario comentarioFromDB = getComentarioOrThrow(id);

        comentarioFromDB.setMensaje(updateComentario.getMensaje());
        if (updateComentario.getClienteId() != null) {
            comentarioFromDB.setCliente(clienteRepository.findById(updateComentario.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado por ID: " + updateComentario.getClienteId())));
        }
        if (publicacionId != null) {
            comentarioFromDB.setPublicacion(publicacionRepository.findById(publicacionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Publicacion no encontrada por ID: " + publicacionId)));
        }
        comentarioFromDB.setFechaActualizacion(LocalDateTime.now());

        return comentarioMapper.toDTO(comentarioRepository.save(comentarioFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        comentarioRepository.delete(getComentarioOrThrow(id));
    }

    private Comentario getComentarioOrThrow(Integer id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado por ID: " + id));
    }
}
