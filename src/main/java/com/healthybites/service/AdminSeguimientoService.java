package com.healthybites.service;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminSeguimientoService {
    List<SeguimientoDTO> getAll();
    Page<SeguimientoDTO> paginate(Pageable pageable);
    SeguimientoDTO findById(Integer id);
    SeguimientoDTO create(Integer metaId, SeguimientoCreateDTO seguimientoCreateDTO);
    SeguimientoDTO update(Integer id, Integer metaId, SeguimientoCreateDTO updateSeguimiento);
    void delete(Integer id);
}
