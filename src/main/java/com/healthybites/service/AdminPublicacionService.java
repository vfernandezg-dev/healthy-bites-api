package com.healthybites.service;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminPublicacionService {
    List<PublicacionDTO> getAll();
    Page<PublicacionDTO> paginate(Pageable pageable);
    PublicacionDTO findById(Integer id);
    PublicacionDTO create(PublicacionCreateDTO publicacionCreateDTO);
    PublicacionDTO update(Integer id, PublicacionCreateDTO updatePublicacion);
    void delete(Integer id);
}
