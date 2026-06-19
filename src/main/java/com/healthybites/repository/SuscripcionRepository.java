package com.healthybites.repository;

import com.healthybites.model.entity.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {
    //Aplicando US de ver historial de pagos
    List<Suscripcion> findByClienteId(Integer clienteId);
}
