package com.healthybites.security.token;

/**
 * El refresh token no permite renovar la sesión: no existe, caducó, fue revocado o se reutilizó. El cliente debe
 * volver a iniciar sesión. El motivo se registra en el log, pero nunca se devuelve al cliente.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String reason) {
        super(reason);
    }
}
