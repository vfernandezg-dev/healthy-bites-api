package com.healthybites.shared.security.ratelimit;

import java.time.Duration;
import java.util.Optional;

/**
 * Como mucho {@code maxEvents} eventos por clave en una ventana deslizante de {@code window}. Se crea con
 * {@link RateLimiters}, que decide dónde se cuentan los eventos: en PostgreSQL (compartido por todas las instancias)
 * o en memoria.
 *
 * <p>Los métodos que registran se llaman fuera de cualquier transacción: la implementación de PostgreSQL usa su
 * propia transacción para que un intento fallido cuente aunque la operación de quien llama se deshaga, y dentro de
 * otra retendría dos conexiones a la vez.
 */
public interface RateLimiter {

    /** Registra un evento si la clave no alcanzó el límite. La comprobación y el registro son atómicos. */
    boolean tryAcquire(String key);

    /** Tiempo hasta que la clave vuelva a tener margen, o vacío si no está limitada. No registra nada. */
    Optional<Duration> retryAfter(String key);

    /** Registra un evento sin comprobar el límite (por ejemplo, un inicio de sesión fallido). */
    void record(String key);

    /** Olvida los eventos de la clave (por ejemplo, tras un inicio de sesión correcto). */
    void reset(String key);
}
