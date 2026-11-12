package com.healthybites.service;

import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;

public interface CheckoutService {

    /** Crea el pedido en PayPal para una suscripción pendiente del usuario y devuelve la URL de aprobación. */
    PaymentOrderResponse createPayment(Integer suscripcionId);

    /** Captura un pedido del usuario actual y activa la suscripción. Repetirlo no cobra ni activa dos veces. */
    PaymentCaptureResponse capturePayment(String orderId);
}
