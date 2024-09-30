package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByCorreo(String correo);
}
