package com.healthybites.repository;

import com.healthybites.model.entity.Habito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitoRepository extends JpaRepository<Habito, Integer> {
    List<Habito> findByClienteId(Integer clienteId);
}
