package com.healthybites.rewards.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Recompensa de un nutricionista: N días seguidos desbloquean un contenido o un plan (exactamente uno). */
@Entity
@Table(name = "rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reward extends BaseEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "required_streak_days", nullable = false)
    private int requiredStreakDays;

    @Column(name = "content_item_id")
    private @Nullable UUID contentItemId;

    @Column(name = "meal_plan_id")
    private @Nullable UUID mealPlanId;

    @Column(nullable = false)
    private boolean active;

    public record Data(
            String title,
            String description,
            int requiredStreakDays,
            @Nullable UUID contentItemId,
            @Nullable UUID mealPlanId) {}

    public static Reward create(UUID authorId, Data data) {
        Reward reward = new Reward();
        reward.authorId = authorId;
        reward.active = true;
        reward.update(data);
        return reward;
    }

    public void update(Data data) {
        this.title = data.title().trim();
        this.description = data.description().trim();
        this.requiredStreakDays = data.requiredStreakDays();
        this.contentItemId = data.contentItemId();
        this.mealPlanId = data.mealPlanId();
    }

    /** Deja de ofrecerse; las rachas completadas conservan su premio. */
    public void deactivate() {
        this.active = false;
    }
}
