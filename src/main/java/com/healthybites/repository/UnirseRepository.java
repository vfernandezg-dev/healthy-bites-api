package com.healthybites.repository;

import com.healthybites.model.entity.Grupo;
import com.healthybites.model.entity.Unirse;
import com.healthybites.model.entity.UnirsePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UnirseRepository extends JpaRepository<Unirse, UnirsePK> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO unirse (cliente, grupos) VALUES (:clienteId, :grupoId)", nativeQuery = true)
    void addClienteToGrupo(@Param("clienteId") Integer clienteId,@Param("grupoId") Integer grupoId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM unirse WHERE cliente = :clienteId AND grupo = :grupoId", nativeQuery = true)
    void removeClienteFromGrupo(@Param("clienteId") Integer clienteId, @Param("grupoId") Integer grupoId);

    @Query("SELECT u FROM Grupo u JOIN Unirse uc ON u.id = uc.grupo WHERE uc.cliente = :clienteId")
    List<Grupo> findGrupoByClienteId(@Param("clienteId") Integer clienteId);
}
