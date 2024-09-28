package com.healthybites.service;

import com.healthybites.model.entity.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminComentarioService {
    List<Comentario> getAll();
    Page<Comentario> paginate(Pageable pageable);
    Comentario findById(Integer id);
    Comentario create(Comentario comentario);
    Comentario update(Integer id, Comentario updateComentario);
    void delete(Integer id);
}
