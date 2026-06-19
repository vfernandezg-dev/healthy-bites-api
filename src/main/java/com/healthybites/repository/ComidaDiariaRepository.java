package com.healthybites.repository;

import com.healthybites.model.entity.ComidaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComidaDiariaRepository extends JpaRepository<ComidaDiaria, Integer> {
    List<ComidaDiaria> findByPlanAlimenticioId(Integer planAlimenticioId);
    Optional<ComidaDiaria> findByIdAndPlanAlimenticioId(Integer comidaDiariaId, Integer planAlimenticioId);
}

