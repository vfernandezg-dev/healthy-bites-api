package com.healthybites.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLTransientConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;

/** Errores de infraestructura que no se pueden provocar fácilmente en un test de integración. */
class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void aPoolWithoutFreeConnectionsAnswers503WithRetryAfter() {
        // Lo que lanza JpaTransactionManager cuando Hikari agota connection-timeout.
        CannotCreateTransactionException exhausted = new CannotCreateTransactionException(
                "Could not open JPA EntityManager for transaction",
                new SQLTransientConnectionException("HikariPool-1 - Connection is not available"));

        assertDatabaseUnavailable(handler.handleDatabaseUnavailable(exhausted));
        assertDatabaseUnavailable(handler.handleDatabaseUnavailable(new CannotGetJdbcConnectionException(
                "Failed to obtain JDBC Connection", new SQLTransientConnectionException("timeout"))));
    }

    private static void assertDatabaseUnavailable(ResponseEntity<ProblemDetail> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("5");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProperties())
                .containsEntry(GlobalExceptionHandler.CODE, "database-unavailable");
    }
}
