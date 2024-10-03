package com.healthybites.repository;

import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.AccesoContenidoPK;
import com.healthybites.model.entity.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccesoContenidoRepository extends JpaRepository<AccesoContenido, AccesoContenidoPK> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO acceso_contenido (cliente, contenido) VALUES (:clienteId, :contenidoId)", nativeQuery = true)
    void addClienteToContenido(@Param("clienteId") Integer clienteId, @Param("contenidoId") Integer contenidoId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM acceso_contenido WHERE cliente = :clienteId AND contenido = :contenidoId", nativeQuery = true)
    void deleteByClienteAndContenido(@Param("clienteId") Integer clienteId, @Param("contenidoId") Integer contenidoId);

    @Query("SELECT c FROM Contenido c JOIN AccesoContenido ac ON c.id = ac.contenido WHERE ac.cliente = :clienteId")
    List<Contenido> findContenidoByCliente(@Param("clienteId") Integer clienteId);
}