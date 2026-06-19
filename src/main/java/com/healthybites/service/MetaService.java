package com.healthybites.service;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;

import java.util.List;

public interface MetaService {

    List<MetaDTO> getAll(Integer id);
    MetaDTO findByIdAndClienteId(Integer metaId, Integer clienteId);
    MetaDTO create(MetaCreateDTO metaCreateDTO);
    MetaDTO update(Integer metaId, Integer clienteId, MetaCreateDTO updatedMetaDTO);
    void delete(Integer metaId, Integer clienteId);
}
