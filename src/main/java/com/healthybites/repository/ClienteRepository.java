
package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByCorreo(String correo);  // Método para buscar por correo
    boolean existsByCorreo(String correo);  // Para verificar si el correo ya está registrado
    Optional<Cliente> findByNombreAndApellido(String nombre, String apellido);
    Optional<Cliente> findByResetPasswordToken(String token);
}
