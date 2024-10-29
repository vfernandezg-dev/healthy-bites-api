package com.healthybites.repository;

import com.healthybites.model.entity.Unirse;
import com.healthybites.model.entity.UnirsePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface UnirseRepository extends JpaRepository<Unirse, UnirsePK> {

    // Esta query es para añadir un cliente a un grupo
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO unirse (cliente_id, grupo_id, fecha_creacion)" +
            "VALUES (:clienteId, :grupoId, :fechaCreacionId)", nativeQuery = true)
    void addClienteToGrupo(@Param("clienteId") Integer clienteId, @Param("grupoId") Integer grupoId, @Param("fechaCreacionId") LocalDateTime fechaCreacion);

    // Esta query es para eliminar un cliente de un grupo
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM unirse WHERE cliente_id = :clienteId AND grupo_id = :grupoId", nativeQuery = true)
    void deleteByClienteAndGrupo(@Param("clienteId") Integer clienteId, @Param("grupoId") Integer grupoId);

    @Query(value = "SELECT COUNT(*) > 0 FROM unirse WHERE cliente_id = :clienteId AND grupo_id = :grupoId", nativeQuery = true)
    boolean existsByClienteIdAndGrupoId(@Param("clienteId") Integer clienteId, @Param("grupoId") Integer grupoId);
}
