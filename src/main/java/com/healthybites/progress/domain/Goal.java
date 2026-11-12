package com.healthybites.progress.domain;

import com.healthybites.shared.domain.BaseEntity;
import com.healthybites.shared.web.ApiException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Meta de peso. ACTIVE → COMPLETED o ABANDONED; cada cambio de estado queda en el historial. */
@Entity
@Table(name = "goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends BaseEntity {

    public enum Status {
        ACTIVE,
        COMPLETED,
        ABANDONED
    }

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private @Nullable String description;

    @Column(name = "target_weight_kg", nullable = false, precision = 5, scale = 1)
    private BigDecimal targetWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "started_on", nullable = false)
    private LocalDate startedOn;

    @Column(name = "completed_at")
    private @Nullable Instant completedAt;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt asc")
    private List<GoalStatusChange> history = new ArrayList<>();

    public static Goal start(
            UUID clientId,
            String title,
            @Nullable String description,
            BigDecimal targetWeightKg,
            LocalDate today,
            Instant now) {
        Goal goal = new Goal();
        goal.clientId = clientId;
        goal.status = Status.ACTIVE;
        goal.startedOn = today;
        goal.update(title, description, targetWeightKg);
        goal.history.add(GoalStatusChange.of(goal, null, Status.ACTIVE, now));
        return goal;
    }

    public void update(String title, @Nullable String description, BigDecimal targetWeightKg) {
        this.title = title.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.targetWeightKg = targetWeightKg;
    }

    public void complete(Instant now) {
        transition(Status.COMPLETED, now);
        this.completedAt = now;
    }

    public void abandon(Instant now) {
        transition(Status.ABANDONED, now);
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public List<GoalStatusChange> getHistory() {
        return Collections.unmodifiableList(history);
    }

    private void transition(Status to, Instant now) {
        if (status != Status.ACTIVE) {
            throw ApiException.conflict("goal-closed", "La meta ya está cerrada");
        }
        history.add(GoalStatusChange.of(this, status, to, now));
        this.status = to;
    }
}
