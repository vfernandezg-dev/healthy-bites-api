package com.healthybites.progress.app;

import com.healthybites.progress.domain.HabitLog;
import com.healthybites.progress.domain.HabitLogRepository;
import com.healthybites.progress.events.HabitLogged;
import com.healthybites.shared.web.ApiException;
import com.healthybites.shared.web.DateRanges;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registros diarios de hábitos. Solo se registra hoy o ayer (para quien lo apunta al día siguiente): rellenar días
 * antiguos de golpe permitía inventarse rachas y ganar recompensas en segundos.
 */
@Service
public class HabitLogService {

    private final HabitLogRepository logs;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public HabitLogService(HabitLogRepository logs, ApplicationEventPublisher events, Clock clock) {
        this.logs = logs;
        this.events = events;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<HabitLog> list(UUID clientId, @Nullable LocalDate from, @Nullable LocalDate to) {
        LocalDate end = to == null ? today() : to;
        LocalDate start = from == null ? end.minusDays(30) : from;
        DateRanges.check(start, end);
        return logs.findByClientIdAndLoggedOnBetweenOrderByLoggedOnDesc(clientId, start, end);
    }

    @Transactional
    public HabitLog log(UUID clientId, @Nullable LocalDate loggedOn, HabitLog.Scores scores) {
        LocalDate day = loggedOn == null ? today() : loggedOn;
        if (day.isAfter(today()) || day.isBefore(today().minusDays(1))) {
            throw ApiException.unprocessable(
                    "log-date-out-of-range", "Solo puedes registrar los hábitos de hoy o de ayer");
        }
        if (logs.existsByClientIdAndLoggedOn(clientId, day)) {
            throw ApiException.conflict("habit-log-exists", "Ya registraste tus hábitos ese día: edítalos");
        }
        HabitLog log = logs.save(HabitLog.of(clientId, day, scores));
        events.publishEvent(new HabitLogged(clientId, day));
        return log;
    }

    @Transactional
    public HabitLog update(UUID logId, UUID clientId, HabitLog.Scores scores) {
        HabitLog log = owned(logId, clientId);
        log.update(scores);
        return log;
    }

    @Transactional
    public void delete(UUID logId, UUID clientId) {
        logs.findByIdAndClientId(logId, clientId).ifPresent(logs::delete);
    }

    private HabitLog owned(UUID logId, UUID clientId) {
        return logs.findByIdAndClientId(logId, clientId)
                .orElseThrow(() -> ApiException.notFound("habit-log-not-found", "El registro no existe"));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
