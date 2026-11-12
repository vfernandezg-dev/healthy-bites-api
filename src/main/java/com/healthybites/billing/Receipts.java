package com.healthybites.billing;

import com.healthybites.shared.domain.Money;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Justificante de un pago capturado, para otros módulos (el correo de confirmación de notifications). */
public interface Receipts {

    /** El justificante si el pago existe y está capturado. */
    Optional<Receipt> find(UUID paymentId);

    /**
     * @param reference referencia del proveedor (el id de la captura de PayPal), la que ve también en su cuenta
     * @param periodEnd hasta cuándo da acceso la suscripción pagada
     */
    record Receipt(
            UUID userId,
            Tier tier,
            Money amount,
            Instant paidAt,
            @Nullable Instant periodEnd,
            String reference) {}
}
