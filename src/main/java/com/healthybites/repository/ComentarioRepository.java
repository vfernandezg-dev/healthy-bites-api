package com.healthybites.repository;

import com.healthybites.model.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    List<Comentario> findByPublicacionId(Integer publicacionId);
    Optional<Comentario> findByIdAndPublicacionId(Integer comentarioId, Integer publicacionId);
}
