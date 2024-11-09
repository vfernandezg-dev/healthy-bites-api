package com.healthybites.repository;

import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.AccesoContenidoPK;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AccesoContenidoRepository extends JpaRepository<AccesoContenido, AccesoContenidoPK> {

    // Esta query es para añadir contenido a cliente
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO acceso_contenido (cliente_id, contenido_id, fecha_creacion)" +
            "VALUES (:clienteId, :contenidoId, :fechaCreacionId)", nativeQuery = true)
    void addContenidoToCliente(@Param("clienteId") Integer clienteId,@Param("contenidoId") Integer contenidoId, @Param("fechaCreacionId") LocalDateTime fechaCreacion);

    // Esta query es para eliminar contenido de cliente
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM acceso_contenido WHERE cliente_id = :clienteId AND contenido_id = :contenidoId", nativeQuery = true)
    void deleteByClienteAndContenido(@Param("clienteId") Integer cliente,@Param("contenidoId") Integer contenido);

    // Esta query es para listar todos los contenidos por cliente
    @Query("SELECT ac.contenido FROM AccesoContenido ac WHERE ac.cliente = :cliente ")
    List<Contenido> findAccesoContenidoByCliente(Cliente cliente);


    @Query("SELECT ac FROM AccesoContenido ac WHERE ac.cliente = :cliente ")
    List<AccesoContenido> findByCliente(Cliente cliente);


}
