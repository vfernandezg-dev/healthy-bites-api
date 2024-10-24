package com.healthybites.repository;

import com.healthybites.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByCorreo(String correo);

    Optional<Usuario> findByCorreo(String correo);
}
