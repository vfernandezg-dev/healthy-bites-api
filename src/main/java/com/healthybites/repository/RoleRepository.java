package com.healthybites.repository;

import com.healthybites.model.enums.ERol;
import org.springframework.data.jpa.repository.JpaRepository;

import com.healthybites.model.entity.Rol;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Rol, Integer> {
    // Busca un rol por su nombre (enum)
    Optional<Rol> findByNombre(ERol nombre);
}
