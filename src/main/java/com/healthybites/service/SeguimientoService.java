package com.healthybites.service;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;

import java.util.List;

public interface SeguimientoService {
    List<SeguimientoDTO> getAll(Integer metaId);
    SeguimientoDTO findByIdAndMetaId(Integer seguimientoId, Integer metaId);
    SeguimientoDTO create(Integer metaId, SeguimientoCreateDTO seguimientoCreateDTO);
    SeguimientoDTO update(Integer metaId, Integer seguimientoId, SeguimientoCreateDTO updatedSeguimientoDTO);
    void delete(Integer seguimientoId, Integer metaId);
}
