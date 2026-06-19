package com.healthybites.service;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.dto.SeguimientoCreateUpdateDTO;
import com.healthybites.dto.SeguimientoDetailsDTO;

import java.util.List;

public interface MetaService {
    List<MetaDTO> getAll();
    List<SeguimientoDetailsDTO> getAllSeguimientosByMetaId(Integer metaId);

    MetaDTO findById(Integer id);

    MetaDTO create(MetaCreateDTO metaCreateDTO);
    SeguimientoDetailsDTO addSeguimientoToMeta(Integer metaId, SeguimientoCreateUpdateDTO seguimientoDTO);

    SeguimientoDetailsDTO getSeguimientoById(Integer metaId, Integer seguimientoId);

    MetaDTO update(Integer id, MetaCreateDTO updatedMetaDTO);
    SeguimientoDetailsDTO updateSeguimiento(Integer metaId, Integer seguimientoId, SeguimientoCreateUpdateDTO seguimientoUpdateDTO); // Actualizar un seguimiento específico


    void delete(Integer id);
    void deleteSeguimiento(Integer metaId, Integer seguimientoId);
}
