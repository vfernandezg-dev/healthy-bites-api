package com.healthybites.repository;

import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.enums.TipoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {
    //Optional<Suscripcion> findByTipoSuscripcion(TipoSuscripcion tipoSuscripcion);

    //List<Suscripcion> findByUsuarioId(Integer usuarioId);

}
