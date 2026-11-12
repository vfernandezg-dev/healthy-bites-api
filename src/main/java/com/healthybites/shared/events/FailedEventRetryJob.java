package com.healthybites.shared.events;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reintenta los eventos persistidos cuyo listener falló, por ejemplo un correo con el SMTP caído. Spring Modulith
 * los guarda como fallidos en {@code event_publication}; cada 15 minutos se vuelven a entregar los que tienen al
 * menos {@link #MIN_AGE} y les quedan intentos. Al arrancar, además, se reintentan todos los pendientes
 * ({@code spring.modulith.events.republish-outstanding-events-on-restart}).
 */
@Component
public class FailedEventRetryJob {

    /** Intentos en total (el primero incluido) antes de dejar el evento para revisarlo a mano. */
    static final int MAX_ATTEMPTS = 5;

    /** Espera mínima desde el último intento: da tiempo a que el servicio caído se recupere. */
    static final Duration MIN_AGE = Duration.ofMinutes(10);

    private final FailedEventPublications failed;

    public FailedEventRetryJob(FailedEventPublications failed) {
        this.failed = failed;
    }

    @Scheduled(initialDelay = 5, fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    public void resubmit() {
        failed.resubmit(
                ResubmissionOptions.defaults().withMinAge(MIN_AGE).withFilter(FailedEventRetryJob::hasAttemptsLeft));
    }

    static boolean hasAttemptsLeft(EventPublication publication) {
        return publication.getCompletionAttempts() < MAX_ATTEMPTS;
    }
}
