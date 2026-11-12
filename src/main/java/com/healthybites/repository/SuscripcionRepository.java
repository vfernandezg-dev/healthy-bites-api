package com.healthybites.repository;

import com.healthybites.model.entity.Suscripcion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {

    /** Cada usuario tiene como mucho una suscripción (restricción única en {@code id_usuario}). */
    Optional<Suscripcion> findByUsuarioId(Integer usuarioId);

    /**
     * Suscripción del pedido de PayPal, bloqueada hasta el final de la transacción: dos capturas simultáneas del
     * mismo pedido se ejecutan una detrás de otra y la segunda ve la suscripción ya pagada.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Suscripcion s where s.paypalOrderId = :orderId")
    Optional<Suscripcion> findByPaypalOrderIdForUpdate(@Param("orderId") String orderId);
}
