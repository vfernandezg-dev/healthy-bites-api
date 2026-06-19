package com.healthybites.service;

import com.healthybites.model.entity.Grupo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminGrupoService {
    List<Grupo> getAll();
    Page<Grupo> paginate(Pageable pageable);
    Grupo findById(Integer id);
    Grupo create(Grupo grupo);
    Grupo update(Integer id, Grupo updateGrupo);
    void delete(Integer id);
}
