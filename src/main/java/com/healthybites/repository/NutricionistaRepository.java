package com.healthybites.repository;

import com.healthybites.model.entity.Nutricionista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutricionistaRepository extends JpaRepository<Nutricionista, Integer> {
    Optional<Nutricionista> findByNombreAndApellido(String nombre, String apellido);

    // Busca un nutricionista por su nombre y apellido
    boolean existsByNombreAndApellido(String nombre, String apellido);

    // Busca un nutricionista por su nombre y apellido, excluyendo el id del usuario
    boolean existsByNombreAndApellidoAndUsuarioIdNot(String nombre, String apellido, Integer usuarioId);
}
