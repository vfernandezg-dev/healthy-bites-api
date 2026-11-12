package com.healthybites.exception;

/**
 * El proveedor de pagos (PayPal) falló o respondió algo que no cuadra con el pedido: 502. El mensaje es para el
 * usuario; los detalles técnicos van al log.
 */
public class PaymentProviderException extends RuntimeException {

    public PaymentProviderException(String message) {
        super(message);
    }

    public PaymentProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
