package com.healthybites.service.impl;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.GrupoMapper;
import com.healthybites.model.entity.Grupo;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.service.AdminGrupoService;
import com.healthybites.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminGrupoServiceImpl implements AdminGrupoService {

    private final GrupoRepository grupoRepository;
    private final ClienteRepository clienteRepository;
    private final GrupoMapper grupoMapper;
    private final GrupoService grupoService;

    @Transactional(readOnly = true)
    @Override
    public List<GrupoDTO> getAll() {
        return grupoRepository.findAll().stream()
                .map(grupoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GrupoDTO> paginate(Pageable pageable) {
        return grupoRepository.findAll(pageable).map(grupoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public GrupoDTO findById(Integer id) {
        return grupoMapper.toDTO(getGrupoOrThrow(id));
    }

    @Transactional
    @Override
    public GrupoDTO create(GrupoCreateDTO grupoCreateDTO) {
        if (grupoCreateDTO.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente creador es obligatorio");
        }
        // Reutiliza la logica del flujo normal (creador, cantidadMiembros=1, fechas)
        return grupoService.create(grupoCreateDTO);
    }

    @Transactional
    @Override
    public GrupoDTO update(Integer id, GrupoCreateDTO updateGrupo) {
        Grupo grupoFromDB = getGrupoOrThrow(id);

        grupoFromDB.setNombre(updateGrupo.getNombre());
        grupoFromDB.setEsPrivado(updateGrupo.isEsPrivado());
        if (updateGrupo.getClienteId() != null) {
            grupoFromDB.setCliente(clienteRepository.findById(updateGrupo.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado por ID: " + updateGrupo.getClienteId())));
        }
        grupoFromDB.setFechaActualizacion(LocalDateTime.now());

        return grupoMapper.toDTO(grupoRepository.save(grupoFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        grupoRepository.delete(getGrupoOrThrow(id));
    }

    private Grupo getGrupoOrThrow(Integer id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado por ID: " + id));
    }
}
