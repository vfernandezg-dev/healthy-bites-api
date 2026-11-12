package com.healthybites.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

/** Un cambio de estado de una meta (inmutable). */
@Entity
@Table(name = "goal_status_changes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalStatusChange {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private Goal.@Nullable Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private Goal.Status toStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    static GoalStatusChange of(Goal goal, Goal.@Nullable Status from, Goal.Status to, Instant at) {
        GoalStatusChange change = new GoalStatusChange();
        change.goal = goal;
        change.fromStatus = from;
        change.toStatus = to;
        change.changedAt = at;
        return change;
    }
}
