package com.healthybites.repository;

import com.healthybites.model.entity.Nutricionista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutricionistaRepository extends JpaRepository<Nutricionista, Integer> {
}
