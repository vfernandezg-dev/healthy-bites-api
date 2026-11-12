package com.healthybites.service.impl;

import com.healthybites.dto.GrupoDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.GrupoMapper;
import com.healthybites.model.entity.Grupo;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.repository.UnirseRepository;
import com.healthybites.service.UnirseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UnirseServiceImpl implements UnirseService {

    private final UnirseRepository unirseRepository;
    private final GrupoRepository grupoRepository;
    private final GrupoMapper grupoMapper;

    /**
     * Alta y contador en la misma transacción. El contador se actualiza con un UPDATE atómico y solo si la
     * membresía cambia de verdad: antes un alta repetida o una baja inexistente lo desajustaban.
     */
    @Override
    @Transactional
    public GrupoDTO addClientToGroup(Integer clientId, Integer groupId) {
        Grupo grupo = findGrupo(groupId);
        if (grupo.getCliente() != null && clientId.equals(grupo.getCliente().getId())) {
            throw new BadRequestException("Ya eres el creador de este grupo");
        }
        if (unirseRepository.existsByClienteIdAndGrupoId(clientId, groupId)) {
            throw new BadRequestException("Ya perteneces a este grupo");
        }

        unirseRepository.addClienteToGrupo(clientId, groupId, LocalDateTime.now());
        grupoRepository.adjustMemberCount(groupId, 1);

        return grupoMapper.toDTO(findGrupo(groupId));
    }

    @Override
    @Transactional
    public void removeClientFromGroup(Integer clientId, Integer groupId) {
        if (unirseRepository.deleteByClienteAndGrupo(clientId, groupId) > 0) {
            grupoRepository.adjustMemberCount(groupId, -1);
        }
    }

    private Grupo findGrupo(Integer groupId) {
        return grupoRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
    }
}
