package com.healthybites.service;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.model.entity.Grupo;

import java.util.List;

public interface GrupoService {
    List<GrupoDTO> getAll(Integer id);
    List<GrupoDTO> getAllGrupos();
    GrupoDTO getGrupoById(Integer grupoId);
    GrupoDTO findByIdAndClienteId(Integer grupoId, Integer clienteId);
    GrupoDTO create(GrupoCreateDTO grupoCreateDTO);
    GrupoDTO update(Integer grupoId, Integer clienteId, GrupoCreateDTO updatedGrupoDTO);
    void delete(Integer grupoId, Integer clienteId);
}
