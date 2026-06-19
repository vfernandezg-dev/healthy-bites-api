package com.healthybites.service;

import com.healthybites.model.entity.Grupo;
import com.healthybites.model.entity.Unirse;

import java.util.List;

public interface UnirseService {
    Unirse addClienteToGrupo(Integer clienteId, Integer grupoId);
    void removeClienteFromGrupo(Integer clienteId, Integer grupoId);
    List<Grupo> getGrupoFromCliente(Integer clienteId);
}
