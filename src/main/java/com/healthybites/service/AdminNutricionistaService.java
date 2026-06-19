package com.healthybites.service;

import com.healthybites.dto.NutricionistaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminNutricionistaService {
    List<NutricionistaDTO> getAll();
    Page<NutricionistaDTO> paginate(Pageable pageable);
    NutricionistaDTO findById(Integer id);
    NutricionistaDTO create( NutricionistaDTO NutricionistaDTO);
    NutricionistaDTO update(Integer id, NutricionistaDTO updateNutricionistaDTO);
    void delete(Integer id);
}
