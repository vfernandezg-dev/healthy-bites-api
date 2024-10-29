package com.healthybites.service.impl;

import com.healthybites.dto.GrupoDTO;
import com.healthybites.mapper.GrupoMapper;
import com.healthybites.model.entity.Grupo;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.repository.UnirseRepository;
import com.healthybites.service.UnirseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UnirseServiceImpl implements UnirseService {

    private final UnirseRepository unirseRepository;
    private final GrupoRepository grupoRepository;
    private final GrupoMapper grupoMapper;

    @Override
    public GrupoDTO addClientToGroup(Integer clientId, Integer groupId) {

        if (unirseRepository.existsByClienteIdAndGrupoId(clientId, groupId)) {
            throw new RuntimeException("El cliente ya está registrado en este grupo");
        }

        LocalDateTime fecha = LocalDateTime.now();
        unirseRepository.addClienteToGrupo(clientId, groupId, fecha);

        // Obtener el grupo por su ID
        Grupo grupo = grupoRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        // Incrementar la cantidad de miembros
        grupo.setCantidadMiembros(grupo.getCantidadMiembros() + 1);

        // Guardar el grupo actualizado
        grupoRepository.save(grupo);

        return grupoMapper.toDTO(grupo);
    }

    @Override
    public void removeClientFromGroup(Integer clientId, Integer groupId) {
        unirseRepository.deleteByClienteAndGrupo(clientId, groupId);

        // Obtener el grupo por su ID
        Grupo grupo = grupoRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        // Decrementar la cantidad de miembros
        grupo.setCantidadMiembros(grupo.getCantidadMiembros() - 1);

        // Guardar el grupo actualizado
        grupoRepository.save(grupo);
    }
}
