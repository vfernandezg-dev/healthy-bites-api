package com.healthybites.billing.domain;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    /**
     * El pago del pedido, bloqueado hasta el final de la transacción: dos capturas simultáneas del mismo pedido (doble
     * clic, dos pestañas) se ejecutan una tras otra y la segunda lo ve ya capturado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.providerOrderId = :orderId")
    Optional<Payment> findByOrderIdForUpdate(@Param("orderId") String orderId);

    /** El pago del pedido, sin bloquearlo (comprobaciones previas a llamar a la pasarela). */
    Optional<Payment> findByProviderOrderId(String providerOrderId);

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    List<Payment> findBySubscriptionIdAndStatus(UUID subscriptionId, PaymentStatus status);

    /** Ingresos capturados desde una fecha, por moneda. */
    @Query("""
            select p.amount.currency as currency, sum(p.amount.amount) as total, count(p) as payments
            from Payment p where p.status = com.healthybites.billing.domain.PaymentStatus.CAPTURED and p.capturedAt >= :since group by p.amount.currency""")
    List<Revenue> revenueSince(@Param("since") Instant since);

    interface Revenue {
        String getCurrency();

        BigDecimal getTotal();

        long getPayments();
    }
}
