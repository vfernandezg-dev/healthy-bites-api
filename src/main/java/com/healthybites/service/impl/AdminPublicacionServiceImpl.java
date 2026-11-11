package com.healthybites.service.impl;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.PublicacionMapper;
import com.healthybites.model.entity.Publicacion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.repository.PublicacionRepository;
import com.healthybites.service.AdminPublicacionService;
import com.healthybites.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminPublicacionServiceImpl implements AdminPublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final ClienteRepository clienteRepository;
    private final GrupoRepository grupoRepository;
    private final PublicacionMapper publicacionMapper;
    private final PublicacionService publicacionService;

    @Transactional(readOnly = true)
    @Override
    public List<PublicacionDTO> getAll() {
        return publicacionRepository.findAll().stream()
                .map(publicacionMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PublicacionDTO> paginate(Pageable pageable) {
        return publicacionRepository.findAll(pageable).map(publicacionMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public PublicacionDTO findById(Integer id) {
        return publicacionMapper.toDTO(getPublicacionOrThrow(id));
    }

    @Transactional
    @Override
    public PublicacionDTO create(PublicacionCreateDTO publicacionCreateDTO) {
        if (publicacionCreateDTO.getClienteId() == null || publicacionCreateDTO.getGrupoId() == null) {
            throw new BadRequestException("clienteId y grupoId son obligatorios");
        }
        // Reutiliza la logica del flujo normal (valida cliente/grupo y asigna fechas)
        return publicacionService.create(publicacionCreateDTO);
    }

    @Transactional
    @Override
    public PublicacionDTO update(Integer id, PublicacionCreateDTO updatePublicacion) {
        Publicacion publicacionFromDB = getPublicacionOrThrow(id);

        publicacionFromDB.setTitulo(updatePublicacion.getTitulo());
        publicacionFromDB.setDescripcion(updatePublicacion.getDescripcion());
        publicacionFromDB.setPublicacionPath(updatePublicacion.getPublicacionPath());
        if (updatePublicacion.getClienteId() != null) {
            publicacionFromDB.setCliente(clienteRepository.findById(updatePublicacion.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado por ID: " + updatePublicacion.getClienteId())));
        }
        if (updatePublicacion.getGrupoId() != null) {
            publicacionFromDB.setGrupo(grupoRepository.findById(updatePublicacion.getGrupoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Grupo no encontrado por ID: " + updatePublicacion.getGrupoId())));
        }
        publicacionFromDB.setFechaActualizacion(LocalDateTime.now());

        return publicacionMapper.toDTO(publicacionRepository.save(publicacionFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        publicacionRepository.delete(getPublicacionOrThrow(id));
    }

    private Publicacion getPublicacionOrThrow(Integer id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion no encontrada por ID: " + id));
    }
}
