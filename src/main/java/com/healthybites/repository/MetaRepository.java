package com.healthybites.repository;


import com.healthybites.model.entity.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaRepository extends JpaRepository<Meta, Integer> {
    Optional<Meta> findById(int id);
    List<Meta> findByClienteId(Integer clienteId);

}
