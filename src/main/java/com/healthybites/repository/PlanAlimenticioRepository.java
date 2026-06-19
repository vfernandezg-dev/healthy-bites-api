package com.healthybites.repository;

import com.healthybites.model.entity.PlanAlimenticio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanAlimenticioRepository extends JpaRepository<PlanAlimenticio, Integer> {
    List<PlanAlimenticio> findByNutricionistaId(Integer nutricionistaId);
    Optional<PlanAlimenticio> findByIdAndNutricionistaId(Integer planId, Integer nutricionistaId);
}
