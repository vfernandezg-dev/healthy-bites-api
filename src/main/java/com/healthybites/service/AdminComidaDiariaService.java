package com.healthybites.service;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminComidaDiariaService {
    List<ComidaDiariaDTO> getAll();
    Page<ComidaDiariaDTO> paginate(Pageable pageable);
    ComidaDiariaDTO findById(Integer id);
    ComidaDiariaDTO create(Integer planAlimenticioId, ComidaDiariaCreateDTO comidaDiariaCreateDTO);
    ComidaDiariaDTO update(Integer id, Integer planAlimenticioId, ComidaDiariaCreateDTO updateComidaDiaria);
    void delete(Integer id);
}
