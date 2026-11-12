package com.healthybites.shared.web;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * Error de negocio que llega al cliente como ProblemDetail (RFC 9457): estado HTTP, un {@code code} estable en
 * kebab-case para que el web-app reaccione sin depender del texto, el {@code detail} en español y, si hace falta,
 * propiedades extra (por ejemplo, {@code requiredTier} en un 403 por suscripción).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private @Nullable Duration retryAfter;

    public ApiException(HttpStatus status, String code, String detail) {
        super(detail);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String code, String detail) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, detail);
    }

    public static ApiException forbidden(String code, String detail) {
        return new ApiException(HttpStatus.FORBIDDEN, code, detail);
    }

    /** Un recurso que no existe o que no es del usuario: se responde igual para no revelar cuál de las dos. */
    public static ApiException notFound(String code, String detail) {
        return new ApiException(HttpStatus.NOT_FOUND, code, detail);
    }

    public static ApiException conflict(String code, String detail) {
        return new ApiException(HttpStatus.CONFLICT, code, detail);
    }

    public static ApiException unprocessable(String code, String detail) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, code, detail);
    }

    public static ApiException tooManyRequests(String detail, Duration retryAfter) {
        ApiException exception = new ApiException(HttpStatus.TOO_MANY_REQUESTS, "too-many-requests", detail);
        exception.retryAfter = retryAfter;
        return exception;
    }

    public static ApiException badGateway(String code, String detail) {
        return new ApiException(HttpStatus.BAD_GATEWAY, code, detail);
    }

    public static ApiException serviceUnavailable(String code, String detail) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, code, detail);
    }

    /** Añade una propiedad al ProblemDetail. */
    public ApiException with(String property, Object value) {
        properties.put(property, value);
        return this;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
