package com.healthybites.progress.app;

import com.healthybites.profiles.ClientProfiles;
import com.healthybites.profiles.ClientProfiles.ClientProfileView;
import com.healthybites.progress.domain.Goal;
import com.healthybites.progress.domain.GoalRepository;
import com.healthybites.progress.domain.GoalStatusChange;
import com.healthybites.progress.domain.WeightEntry;
import com.healthybites.progress.domain.WeightEntryRepository;
import com.healthybites.progress.events.GoalCompleted;
import com.healthybites.shared.web.ApiException;
import com.healthybites.shared.web.DateRanges;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Metas de peso del cliente, sus pesajes y la recomendación de peso saludable. Lo ajeno responde 404. */
@Service
public class GoalService {

    static final BigDecimal HEALTHY_BMI_MIN = new BigDecimal("18.5");
    static final BigDecimal HEALTHY_BMI_MAX = new BigDecimal("24.9");
    /** Ritmos semanales prudentes: perder 0,5 kg o ganar 0,25 kg por semana. */
    static final BigDecimal SAFE_LOSS_PER_WEEK = new BigDecimal("0.5");

    static final BigDecimal SAFE_GAIN_PER_WEEK = new BigDecimal("0.25");

    /** Límites que PostgreSQL puede representar (LocalDate.MIN y MAX no lo son). */
    private static final LocalDate EARLIEST = LocalDate.of(1900, 1, 1);

    private static final LocalDate LATEST = LocalDate.of(9999, 12, 31);

    private final GoalRepository goals;
    private final WeightEntryRepository weights;
    private final ClientProfiles profiles;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public GoalService(
            GoalRepository goals,
            WeightEntryRepository weights,
            ClientProfiles profiles,
            ApplicationEventPublisher events,
            Clock clock) {
        this.goals = goals;
        this.weights = weights;
        this.profiles = profiles;
        this.events = events;
        this.clock = clock;
    }

    public record GoalData(String title, @Nullable String description, BigDecimal targetWeightKg) {}

    public record WeightData(
            LocalDate measuredOn,
            BigDecimal weightKg,
            @Nullable String notes) {}

    /** Recomendación a partir de la altura del perfil y del último pesaje (o del peso del perfil). */
    public record Recommendation(
            BigDecimal heightCm,
            @Nullable BigDecimal currentWeightKg,
            BigDecimal targetWeightKg,
            BigDecimal healthyMinKg,
            BigDecimal healthyMaxKg,
            @Nullable BigDecimal currentBmi,
            BigDecimal targetBmi,
            boolean targetInHealthyRange,
            @Nullable BigDecimal weeklyChangeKg,
            @Nullable Integer estimatedWeeks,
            String message) {}

    @Transactional(readOnly = true)
    public List<Goal> list(UUID clientId, Goal.@Nullable Status status) {
        return status == null
                ? goals.findByClientIdOrderByCreatedAtDesc(clientId)
                : goals.findByClientIdAndStatusOrderByCreatedAtDesc(clientId, status);
    }

    @Transactional(readOnly = true)
    public Goal get(UUID goalId, UUID clientId) {
        return owned(goalId, clientId);
    }

    @Transactional
    public Goal create(UUID clientId, GoalData data) {
        if (goals.existsByClientIdAndStatus(clientId, Goal.Status.ACTIVE)) {
            throw ApiException.conflict(
                    "active-goal-exists", "Ya tienes una meta activa: complétala o abandónala antes de crear otra");
        }
        return goals.save(Goal.start(
                clientId, data.title(), data.description(), data.targetWeightKg(), today(), clock.instant()));
    }

    @Transactional
    public Goal update(UUID goalId, UUID clientId, GoalData data) {
        Goal goal = owned(goalId, clientId);
        goal.update(data.title(), data.description(), data.targetWeightKg());
        return goal;
    }

    @Transactional
    public Goal complete(UUID goalId, UUID clientId) {
        Goal goal = owned(goalId, clientId);
        goal.complete(clock.instant());
        events.publishEvent(new GoalCompleted(goal.requireId(), clientId));
        return goal;
    }

    @Transactional
    public Goal abandon(UUID goalId, UUID clientId) {
        Goal goal = owned(goalId, clientId);
        goal.abandon(clock.instant());
        return goal;
    }

    @Transactional(readOnly = true)
    public List<GoalStatusChange> history(UUID goalId, UUID clientId) {
        List<GoalStatusChange> history = owned(goalId, clientId).getHistory();
        history.size();
        return history;
    }

    /** Todos los pesajes de la meta en el rango (para el gráfico), del más antiguo al más reciente. */
    @Transactional(readOnly = true)
    public List<WeightEntry> weights(UUID goalId, UUID clientId, @Nullable LocalDate from, @Nullable LocalDate to) {
        DateRanges.check(from, to);
        Goal goal = owned(goalId, clientId);
        if (from == null && to == null) {
            return weights.findByGoalIdOrderByMeasuredOnAsc(goal.requireId());
        }
        return weights.findByGoalIdAndMeasuredOnBetweenOrderByMeasuredOnAsc(
                goal.requireId(), from == null ? EARLIEST : from, to == null ? LATEST : to);
    }

    @Transactional
    public WeightEntry addWeight(UUID goalId, UUID clientId, WeightData data) {
        Goal goal = activeOwned(goalId, clientId);
        if (data.measuredOn().isAfter(today())) {
            throw ApiException.unprocessable("future-date", "La fecha del pesaje no puede ser futura");
        }
        if (weights.existsByGoalIdAndMeasuredOn(goal.requireId(), data.measuredOn())) {
            throw ApiException.conflict("weight-entry-exists", "Ya hay un pesaje ese día: edítalo");
        }
        return weights.save(WeightEntry.of(goal.requireId(), data.measuredOn(), data.weightKg(), data.notes()));
    }

    @Transactional
    public WeightEntry updateWeight(UUID goalId, UUID entryId, UUID clientId, WeightData data) {
        Goal goal = owned(goalId, clientId);
        WeightEntry entry = weights.findByIdAndGoalId(entryId, goal.requireId())
                .orElseThrow(() -> ApiException.notFound("weight-entry-not-found", "El pesaje no existe"));
        entry.update(data.weightKg(), data.notes());
        return entry;
    }

    @Transactional
    public void deleteWeight(UUID goalId, UUID entryId, UUID clientId) {
        Goal goal = owned(goalId, clientId);
        weights.findByIdAndGoalId(entryId, goal.requireId()).ifPresent(weights::delete);
    }

    @Transactional(readOnly = true)
    public Recommendation recommendation(UUID goalId, UUID clientId) {
        Goal goal = owned(goalId, clientId);
        ClientProfileView profile = profiles.find(clientId).orElse(null);
        if (profile == null || profile.heightCm() == null) {
            throw ApiException.conflict(
                    "profile-incomplete", "Añade tu altura en el perfil para calcular la recomendación");
        }
        BigDecimal heightCm = profile.heightCm();
        BigDecimal heightM2 = heightCm.divide(BigDecimal.valueOf(100)).pow(2);
        BigDecimal current = weights.findFirstByGoalIdOrderByMeasuredOnDesc(goal.requireId())
                .map(WeightEntry::getWeightKg)
                .orElse(profile.weightKg());
        BigDecimal target = goal.getTargetWeightKg();
        BigDecimal min = HEALTHY_BMI_MIN.multiply(heightM2).setScale(1, RoundingMode.HALF_UP);
        BigDecimal max = HEALTHY_BMI_MAX.multiply(heightM2).setScale(1, RoundingMode.HALF_UP);
        BigDecimal targetBmi = target.divide(heightM2, 1, RoundingMode.HALF_UP);
        boolean healthy = target.compareTo(min) >= 0 && target.compareTo(max) <= 0;

        BigDecimal currentBmi = current == null ? null : current.divide(heightM2, 1, RoundingMode.HALF_UP);
        BigDecimal weekly = null;
        Integer weeks = null;
        if (current != null) {
            BigDecimal difference = target.subtract(current);
            weekly = difference.signum() < 0 ? SAFE_LOSS_PER_WEEK.negate() : SAFE_GAIN_PER_WEEK;
            weeks = difference.signum() == 0
                    ? 0
                    : difference
                            .abs()
                            .divide(weekly.abs(), 0, RoundingMode.CEILING)
                            .intValue();
        }
        return new Recommendation(
                heightCm,
                current,
                target,
                min,
                max,
                currentBmi,
                targetBmi,
                healthy,
                weekly,
                weeks,
                message(healthy, target, min, max, weeks));
    }

    private static String message(
            boolean healthy, BigDecimal target, BigDecimal min, BigDecimal max, @Nullable Integer weeks) {
        String range = "Para tu altura, el rango saludable va de " + min + " a " + max + " kg.";
        if (!healthy) {
            return range + " Tu objetivo (" + target + " kg) queda fuera: coméntalo con tu nutricionista.";
        }
        if (weeks == null) {
            return range + " Registra un pesaje para estimar cuánto te falta.";
        }
        return weeks == 0
                ? range + " Ya estás en tu objetivo: ¡mantenlo!"
                : range + " A un ritmo prudente, llegarías en unas " + weeks + " semanas.";
    }

    private Goal owned(UUID goalId, UUID clientId) {
        return goals.findByIdAndClientId(goalId, clientId)
                .orElseThrow(() -> ApiException.notFound("goal-not-found", "La meta no existe"));
    }

    private Goal activeOwned(UUID goalId, UUID clientId) {
        Goal goal = owned(goalId, clientId);
        if (!goal.isActive()) {
            throw ApiException.conflict("goal-closed", "La meta está cerrada: no admite pesajes nuevos");
        }
        return goal;
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
