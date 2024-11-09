package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Racha;
import com.healthybites.model.entity.RachaPK;
import com.healthybites.model.entity.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RachaRepository extends JpaRepository<Racha, RachaPK> {

    // Este query es para agregar una recompenza a un cliente
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO racha (cliente_id, recompensa_id, contador_dias, entregada, ultima_fecha_registro)" +
            "VALUES (:clienteId, :recompensaId, :contadorDias, :entregada, :ultimaFechaRegistro)", nativeQuery = true)
    void addRecompensaToCliente(@Param("clienteId") Integer clienteId,
                                @Param("recompensaId") Integer recompensaId,
                                @Param("contadorDias") Integer contadorDias,
                                @Param("entregada") boolean entregada,
                                @Param("ultimaFechaRegistro") LocalDateTime ultimaFechaRegistro);

    // Este query es para eliminar una recompenza de un cliente
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM racha WHERE cliente_id = :clienteId AND recompensa_id = :recompensaId", nativeQuery = true)
    void deleteByClienteAndRecompensa(@Param("clienteId") Integer clienteId, @Param("recompensaId") Integer recompensaId);

    // Este query es para listar todas las recompensas por cliente
    @Query("SELECT r.recompensa FROM Racha r WHERE r.cliente = :cliente ")
    List<Recompensa> findRachaByCliente(Cliente cliente);

    // Este query es para verificar si un cliente tiene acceso a una recompensa
    @Query("SELECT COUNT(r) > 0 FROM Racha r WHERE r.cliente.id = :clienteId AND r.recompensa.id = :recompensaId")
    boolean existsByClienteIdAndRecompensaId(@Param("clienteId") Integer clienteId, @Param("recompensaId") Integer recompensaId);

    @Query("SELECT r FROM Racha r WHERE r.cliente.id = :clienteId")
    List<Racha> findByCliente(@Param("clienteId") Integer clienteId);

}
