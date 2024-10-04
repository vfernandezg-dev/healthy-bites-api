package com.healthybites.repository;

import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.model.enums.PlanObjetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanAlimenticioRepository extends JpaRepository<PlanAlimenticio, Integer> {

    Optional<PlanAlimenticio> findByPlanObjetivo(PlanObjetivo planObjetivo);
}
