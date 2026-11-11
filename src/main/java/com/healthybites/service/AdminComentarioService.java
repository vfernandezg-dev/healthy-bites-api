package com.healthybites.service;

import com.healthybites.dto.ComentarioCreateDTO;
import com.healthybites.dto.ComentarioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminComentarioService {
    List<ComentarioDTO> getAll();
    Page<ComentarioDTO> paginate(Pageable pageable);
    ComentarioDTO findById(Integer id);
    ComentarioDTO create(Integer publicacionId, ComentarioCreateDTO comentarioCreateDTO);
    ComentarioDTO update(Integer id, Integer publicacionId, ComentarioCreateDTO updateComentario);
    void delete(Integer id);
}
