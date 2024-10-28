package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Integer> {
    List<Grupo> findByClienteId(Integer clienteId);
    Optional<Grupo> findByIdAndClienteId(Integer grupoId, Integer clienteId);
}
