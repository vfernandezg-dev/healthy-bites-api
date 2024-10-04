package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByCorreo(String correo);

    Optional<Cliente> findByNombreAndApellido(String nombre, String apellido);
}
