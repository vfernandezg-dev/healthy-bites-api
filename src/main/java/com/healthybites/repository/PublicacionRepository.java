package com.healthybites.repository;

import com.healthybites.model.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
    List<Publicacion> findByClienteId(Integer clienteId);
    Optional<Publicacion> findByIdAndClienteId(Integer publicacionId, Integer clienteId);
}
