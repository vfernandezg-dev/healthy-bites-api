package com.healthybites.service;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDTO;

import java.util.List;

public interface ContenidoService {
    List<ContenidoDTO> getAll();
    List<ContenidoDTO> getAllByNutricionistaId(Integer nutricionistaId);
    ContenidoDTO findById(Integer id);
    ContenidoDTO create(ContenidoCreateUpdateDTO contenidoCreateUpdateDTO);
    ContenidoDTO update(Integer id, ContenidoCreateUpdateDTO updateContenidoDTO);
    void delete(Integer id);
}
