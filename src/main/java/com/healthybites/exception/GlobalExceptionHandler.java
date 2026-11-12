package com.healthybites.exception;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Todas las respuestas de error siguen RFC 9457 ({@link ProblemDetail}). Nunca se devuelve el mensaje de una
 * excepción inesperada: se registra en el log y el cliente recibe un texto genérico.
 *
 * <p>Compatibilidad temporal: el web-app actual muestra {@code error.message}, así que cada problema lleva
 * también {@code message} con el mismo texto que {@code detail}. Se elimina en la fase 3.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static ProblemDetail problem(HttpStatusCode status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("message", detail);
        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    ProblemDetail handleBadRequest(BadRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Validaciones de negocio que lanzan los servicios (correo ya registrado, nombre duplicado...). Antes: 500.
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Login fallido. Antes caía en el handler genérico y respondía 500. El mensaje es el mismo exista o no la
    // cuenta, para no revelar qué correos están registrados.
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    // Sin esto, el handler genérico convertía los "Access Denied" de @PreAuthorize en 500.
    @ExceptionHandler({AccessDeniedException.class, RoleNotFoundException.class})
    ProblemDetail handleAccessDenied(Exception ex) {
        return problem(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso");
    }

    @ExceptionHandler(TooManyRequestsException.class)
    ProblemDetail handleTooManyRequests(TooManyRequestsException ex) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes");
    }

    // Fallos del servidor de correo: 503 con un mensaje genérico (no se exponen detalles del SMTP).
    @ExceptionHandler({MailException.class, MessagingException.class})
    ProblemDetail handleMail(Exception ex) {
        log.warn("No se pudo enviar un correo: {}", ex.getMessage());
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "No pudimos enviar el correo en este momento. Inténtalo más tarde.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Se produjo un error inesperado. Inténtalo más tarde.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of("field", error.getField(), "message", String.valueOf(error.getDefaultMessage())))
                .toList();
        String detail = errors.stream()
                .map(error -> error.get("field") + ": " + error.get("message"))
                .collect(Collectors.joining(", "));

        ProblemDetail problem = problem(HttpStatus.UNPROCESSABLE_CONTENT, detail);
        problem.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(problem);
    }

    // Errores estándar de Spring MVC (JSON mal formado, método no permitido...): ya son ProblemDetail; se les
    // añade "message" por la misma compatibilidad.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem && problem.getDetail() != null) {
            problem.setProperty("message", problem.getDetail());
        }
        return response;
    }
}
