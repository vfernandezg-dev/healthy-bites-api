package com.healthybites.service;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.model.entity.Grupo;

import java.util.List;

public interface GrupoService {
    List<GrupoDTO> getAll(Integer id);
    /** Directorio: grupos públicos y los privados que el usuario creó o a los que pertenece. */
    List<GrupoDTO> getAllGrupos(Integer usuarioId);
    GrupoDTO getGrupoById(Integer grupoId);
    GrupoDTO findByIdAndClienteId(Integer grupoId, Integer clienteId);
    GrupoDTO create(GrupoCreateDTO grupoCreateDTO);
    GrupoDTO update(Integer grupoId, Integer clienteId, GrupoCreateDTO updatedGrupoDTO);
    void delete(Integer grupoId, Integer clienteId);
}
