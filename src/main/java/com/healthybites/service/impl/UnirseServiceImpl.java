package com.healthybites.service.impl;

import com.healthybites.model.entity.Grupo;
import com.healthybites.model.entity.Unirse;
import com.healthybites.repository.UnirseRepository;
import com.healthybites.service.UnirseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnirseServiceImpl implements UnirseService {

    private final UnirseRepository unirseRepository;

    @Override
    public Unirse addClienteToGrupo(Integer clienteId, Integer grupoId) {

        Unirse unirse = new Unirse();
        unirse.setCliente(clienteId);
        unirse.setGrupo(grupoId);
        return unirseRepository.save(unirse);
    }

    @Override
    public void removeClienteFromGrupo(Integer clienteId, Integer grupoId) {
        unirseRepository.removeClienteFromGrupo(clienteId, grupoId);
    }

    @Override
    public List<Grupo> getGrupoFromCliente(Integer clienteId) {
        return unirseRepository.findGrupoByClienteId(clienteId);
    }
}
