package com.healthybites.exception;

/** La petición choca con el estado actual del recurso (por ejemplo, pagar una suscripción ya pagada): 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
