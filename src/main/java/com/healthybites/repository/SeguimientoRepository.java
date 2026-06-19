package com.healthybites.repository;

import com.healthybites.model.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {
}
