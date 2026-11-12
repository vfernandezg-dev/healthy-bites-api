package com.healthybites.billing.app;

import com.healthybites.shared.domain.Money;
import org.jspecify.annotations.Nullable;

/**
 * Pasarela de pago (puerto de salida). La implementa {@code infra.PayPalClient}; la lógica de suscripciones no
 * depende de PayPal ni de HTTP.
 *
 * <ul>
 *   <li>Ambas operaciones son idempotentes por su referencia: repetirlas devuelve el mismo pedido o la misma
 *       captura, sin cobrar dos veces.</li>
 *   <li>Pueden tardar segundos: se llaman siempre fuera de una transacción de base de datos, para no retener una
 *       conexión ni un bloqueo mientras se espera al proveedor.</li>
 *   <li>Un fallo del proveedor llega como {@code ApiException} 502 ({@code payment-provider-error}) o 503 (sin
 *       credenciales).</li>
 * </ul>
 */
public interface PaymentGateway {

    CreatedOrder createOrder(String referenceId, String description, Money amount, String returnUrl, String cancelUrl);

    CapturedOrder captureOrder(String orderId);

    /** Pedido creado: su id y la URL donde el comprador lo aprueba. */
    record CreatedOrder(String id, String approvalUrl) {}

    /** Resultado de la captura. {@code captured} es lo que de verdad se cobró. */
    record CapturedOrder(
            String status,
            @Nullable String referenceId,
            @Nullable String captureId,
            @Nullable Money captured) {

        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }
    }
}
