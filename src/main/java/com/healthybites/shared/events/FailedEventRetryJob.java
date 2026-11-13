package com.healthybites.shared.events;

import java.time.Duration;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reintenta los eventos persistidos cuyo listener falló, por ejemplo un correo con el SMTP caído. Spring Modulith
 * los guarda como fallidos en {@code event_publication}; cada hora (en el minuto 5, con las demás tareas horarias, para
 * despertar la base de datos una sola vez) se vuelven a entregar los que tienen al menos {@link #MIN_AGE} y les
 * quedan intentos.
 *
 * <p>Con varias instancias corre en una sola a la vez ({@code @SchedulerLock}). Las publicaciones que se quedan a
 * medias (una instancia que se cae mientras procesa un evento) las marca como fallidas el monitor de Spring Modulith
 * ({@code spring.modulith.events.staleness.*}) y este job las reintenta. Por eso no se reenvían todas al arrancar:
 * con varias réplicas se duplicarían eventos que otra instancia está procesando.
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

    @Scheduled(cron = "0 5 * * * *")
    @SchedulerLock(name = "failed-event-retry", lockAtMostFor = "PT10M")
    public void resubmit() {
        failed.resubmit(
                ResubmissionOptions.defaults().withMinAge(MIN_AGE).withFilter(FailedEventRetryJob::hasAttemptsLeft));
    }

    static boolean hasAttemptsLeft(EventPublication publication) {
        return publication.getCompletionAttempts() < MAX_ATTEMPTS;
    }
}
