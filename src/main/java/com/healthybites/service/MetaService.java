package com.healthybites.service;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.dto.SeguimientoCreateUpdateDTO;
import com.healthybites.dto.SeguimientoDetailsDTO;

import java.util.List;

public interface MetaService {

    List<MetaDTO> getAll(Integer id);
    MetaDTO findByIdAndClienteId(Integer metaId, Integer clienteId);
    MetaDTO create(MetaCreateDTO metaCreateDTO);
    MetaDTO update(Integer metaId, Integer clienteId, MetaCreateDTO updatedMetaDTO);
    void delete(Integer metaId, Integer clienteId);

    //----------------------------------------

    /*List<MetaDTO> getAll();
    MetaDTO findById(Integer id);
    MetaDTO create(MetaCreateDTO metaCreateDTO);
    MetaDTO update(Integer id, MetaCreateDTO updatedMetaDTO);
    void delete(Integer id);
    */

    //----------------------------------------

    /*List<SeguimientoDetailsDTO> getAllSeguimientosByMetaId(Integer metaId);
    SeguimientoDetailsDTO getSeguimientoById(Integer metaId, Integer seguimientoId);
    SeguimientoDetailsDTO addSeguimientoToMeta(Integer metaId, SeguimientoCreateUpdateDTO seguimientoDTO);
    SeguimientoDetailsDTO updateSeguimiento(Integer metaId, Integer seguimientoId, SeguimientoCreateUpdateDTO seguimientoUpdateDTO); // Actualizar un seguimiento específico
    void deleteSeguimiento(Integer metaId, Integer seguimientoId);
*/
}
