package com.healthybites.service;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminGrupoService {
    List<GrupoDTO> getAll();
    Page<GrupoDTO> paginate(Pageable pageable);
    GrupoDTO findById(Integer id);
    GrupoDTO create(GrupoCreateDTO grupoCreateDTO);
    GrupoDTO update(Integer id, GrupoCreateDTO updateGrupo);
    void delete(Integer id);
}
