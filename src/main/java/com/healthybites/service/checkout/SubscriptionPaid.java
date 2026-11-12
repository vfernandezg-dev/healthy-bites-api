package com.healthybites.service.checkout;

/** Se ha cobrado una suscripción. Lo escucha el envío del correo de confirmación (después del commit). */
public record SubscriptionPaid(String email, String tipo, String total) {
}
