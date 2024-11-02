package com.healthybites.service;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;

import java.util.List;

public interface ComidaDiariaService {

    List<ComidaDiariaDTO> getAll(Integer planId);
    ComidaDiariaDTO findByIdAndPlanId(Integer comidaDiariaId, Integer planId);
    ComidaDiariaDTO create(Integer planId, ComidaDiariaCreateDTO comidaDiariaCreateDTO);
    ComidaDiariaDTO update(Integer planId, Integer comidaDiariaId, ComidaDiariaCreateDTO updatedComidaDiariaDTO);
    void delete(Integer planId, Integer comidaDiariaId);
}
