package com.healthybites.exception;

import java.time.Duration;
import java.util.Optional;

/** 429: se superó un límite de peticiones. Si se conoce la espera, la respuesta incluye {@code Retry-After}. */
public class TooManyRequestsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyRequestsException(String message) {
        this(message, null);
    }

    public TooManyRequestsException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
