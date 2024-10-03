package com.healthybites.service;

import com.healthybites.model.entity.Nutricionista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminNutricionistaService {
    List<Nutricionista> getAll();
    Page<Nutricionista> paginate(Pageable pageable);
    Nutricionista findById(Integer id);
    Nutricionista create(Nutricionista nutricionista);
    Nutricionista update(Integer id, Nutricionista updateNutricionista);
    void delete(Integer id);
}
