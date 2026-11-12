package com.healthybites.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Todos los errores salen como ProblemDetail (RFC 9457) con un {@code code} estable. Los de validación llevan
 * además {@code errors: [{field, message}]} con los mensajes en español, para mostrarlos junto a cada campo.
 *
 * <p>Nunca se devuelve el mensaje de una excepción inesperada: se registra en el log y el cliente recibe un 500
 * genérico.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String CODE = "code";

    public static ProblemDetail problem(HttpStatusCode status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(CODE, code);
        return problem;
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> handleApi(ApiException ex) {
        ProblemDetail problem = problem(ex.getStatus(), ex.getCode(), String.valueOf(ex.getMessage()));
        ex.getProperties().forEach(problem::setProperty);
        ResponseEntity.BodyBuilder response = ResponseEntity.status(ex.getStatus());
        ex.getRetryAfter().ifPresent(wait -> response.header(HttpHeaders.RETRY_AFTER, retryAfterSeconds(wait)));
        return response.body(problem);
    }

    // Mismo mensaje exista o no la cuenta (o esté desactivada): no revela qué correos están registrados.
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "bad-credentials", "Correo o contraseña incorrectos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "No tienes permisos para acceder a este recurso");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleConcurrentModification(OptimisticLockingFailureException ex) {
        return problem(
                HttpStatus.CONFLICT,
                "concurrent-modification",
                "Otro cambio se guardó antes que el tuyo. Recarga e inténtalo de nuevo.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        log.warn(
                "Conflicto de integridad de datos: {}",
                ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "conflict", "La operación entra en conflicto con datos existentes");
    }

    @ExceptionHandler(MailException.class)
    ProblemDetail handleMail(MailException ex) {
        log.warn("No se pudo enviar un correo: {}", ex.getMessage());
        return problem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "mail-unavailable",
                "No pudimos enviar el correo en este momento. Inténtalo más tarde.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal-error",
                "Se produjo un error inesperado. Inténtalo más tarde.");
    }

    // Bean Validation del cuerpo: 422 con los errores de cada campo.
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(Map.of("field", error.getField(), "message", messageOf(error)));
        }
        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> errors.add(Map.of("field", error.getObjectName(), "message", messageOf(error))));
        return ResponseEntity.unprocessableContent().body(validationProblem(errors));
    }

    // Validación de parámetros (@RequestParam, @PathVariable) con restricciones: 422 con el mismo formato.
    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();
        ex.getParameterValidationResults()
                .forEach(result -> result.getResolvableErrors()
                        .forEach(error -> errors.add(Map.of(
                                "field",
                                        String.valueOf(
                                                result.getMethodParameter().getParameterName()),
                                "message", messageOf(error)))));
        return ResponseEntity.unprocessableContent().body(validationProblem(errors));
    }

    /** Errores estándar de Spring MVC (404, 405, 415, JSON mal formado...): se les añade un {@code code}. */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            if (problem.getProperties() == null || !problem.getProperties().containsKey(CODE)) {
                problem.setProperty(CODE, defaultCode(statusCode));
            }
        }
        return response;
    }

    private static ProblemDetail validationProblem(List<Map<String, String>> errors) {
        ProblemDetail problem =
                problem(HttpStatus.UNPROCESSABLE_CONTENT, "validation-failed", "Revisa los datos del formulario");
        problem.setProperty("errors", errors);
        return problem;
    }

    private static String messageOf(MessageSourceResolvable error) {
        return error.getDefaultMessage() == null ? "Valor no válido" : error.getDefaultMessage();
    }

    private static String defaultCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "bad-request";
            case 404 -> "not-found";
            case 405 -> "method-not-allowed";
            case 406 -> "not-acceptable";
            case 413 -> "payload-too-large";
            case 415 -> "unsupported-media-type";
            default -> status.is5xxServerError() ? "internal-error" : "error";
        };
    }

    /** Segundos de espera, redondeados hacia arriba (RFC 9110, Retry-After). */
    private static String retryAfterSeconds(Duration wait) {
        return String.valueOf(Math.max(1, (wait.toMillis() + 999) / 1000));
    }
}
