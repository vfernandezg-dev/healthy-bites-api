package com.healthybites.repository;

import com.healthybites.model.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaRepository extends JpaRepository<Meta, Integer> {
    List<Meta> findByClienteId(Integer clienteId);
    Optional<Meta> findByIdAndClienteId(Integer id, Integer clienteId);
}
