package com.healthybites.service;

import com.healthybites.dto.ComentarioCreateDTO;
import com.healthybites.dto.ComentarioDTO;

import java.util.List;

public interface ComentarioService {
    List<ComentarioDTO> getAll(Integer publicacionId);
    ComentarioDTO findByIdAndPublicacionId(Integer comentarioId, Integer publicacionId);
    ComentarioDTO create(Integer publicacionId, ComentarioCreateDTO comentarioCreateDTO);
    ComentarioDTO update(Integer comentarioId, Integer publicacionId, ComentarioCreateDTO updatedComentarioDTO);
    void delete(Integer comentarioId, Integer publicacionId);
}
