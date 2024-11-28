package com.healthybites.service;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SeguimientoService {
    List<SeguimientoDTO> getAll(Integer metaId);
    List<SeguimientoDTO> getAllSeguimientosByMetaIdAndClienteId(Integer metaId, Integer clienteId);
    Page<SeguimientoDTO> paginateSeguimientosByMetaIdAndClienteId(Integer metaId, Integer clienteId, Pageable pageable);
    Page<SeguimientoDTO> paginate(Pageable pageable);
    SeguimientoDTO findByIdAndMetaId(Integer seguimientoId, Integer metaId);
    SeguimientoDTO create(Integer metaId, SeguimientoCreateDTO seguimientoCreateDTO);
    SeguimientoDTO update(Integer metaId, Integer seguimientoId, SeguimientoCreateDTO updatedSeguimientoDTO);
    void delete(Integer seguimientoId, Integer metaId);
}
