package com.healthybites.service;

import com.healthybites.dto.ComidaDiariaCreateUpdateDTO;
import com.healthybites.dto.ComidaDiariaDetailsDTO;

import java.util.List;

public interface ComidaDiariaService {
    List<ComidaDiariaDetailsDTO> getAll();
    ComidaDiariaDetailsDTO findById(Integer id);
    ComidaDiariaDetailsDTO create(ComidaDiariaCreateUpdateDTO comidaDiariaCreateUpdateDTO);
    ComidaDiariaDetailsDTO update(Integer id, ComidaDiariaCreateUpdateDTO updateComidaDiariaDTO);
    void delete(Integer id);
}
