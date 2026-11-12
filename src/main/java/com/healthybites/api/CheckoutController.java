package com.healthybites.api;

import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;
import com.healthybites.service.CheckoutService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/checkout")
@PreAuthorize("hasRole('CLIENTE')")
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Crea el pedido en PayPal y devuelve la URL de aprobación. Las URLs de vuelta las pone el servidor: si un
     * cliente antiguo aún envía {@code returnUrl} o {@code cancelUrl}, se ignoran.
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('CLIENTE') and @owner.ownsSuscripcion(#suscripcionId)")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(@RequestParam Integer suscripcionId) {
        return ResponseEntity.ok(checkoutService.createPayment(suscripcionId));
    }

    /** Vuelta de PayPal: captura el pedido si es del usuario. Repetirlo no cobra dos veces. */
    @PostMapping("/capture")
    public ResponseEntity<PaymentCaptureResponse> capturePayment(@RequestParam @Size(max = 64) String orderId) {
        PaymentCaptureResponse response = checkoutService.capturePayment(orderId);
        return ResponseEntity.status(response.isCompleted() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }
}
