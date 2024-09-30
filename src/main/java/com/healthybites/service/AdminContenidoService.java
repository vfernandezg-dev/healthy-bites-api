package com.healthybites.service;

import com.healthybites.model.entity.Contenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminContenidoService {
    List<Contenido> getAll();
    Page<Contenido> paginate(Pageable pageable);
    Contenido findById(Integer id);
    Contenido create(Contenido contenido);
    Contenido update(Integer id, Contenido updateContenido);
    void delete(Integer id);
}
