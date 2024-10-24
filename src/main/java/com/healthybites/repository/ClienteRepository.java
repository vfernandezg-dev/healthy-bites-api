package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByNombreAndApellido(String nombre, String apellido);

    // Busca un cliente por su nombre y apellido
    boolean existsByNombreAndApellido(String nombre, String apellido);

    // Busca un cliente por su nombre y apellido, excluyendo el id del usuario
    boolean existsByNombreAndApellidoAndUsuarioIdNot(String nombre, String apellido, Integer usuarioId);
}
