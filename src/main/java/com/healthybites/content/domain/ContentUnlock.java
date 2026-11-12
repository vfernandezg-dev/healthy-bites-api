package com.healthybites.content.domain;

import com.healthybites.content.domain.ContentEnums.UnlockSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

/** Acceso a un contenido de pago sin la suscripción (por ejemplo, ganado con una recompensa). Es permanente. */
@Entity
@Table(name = "content_unlocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentUnlock {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @Column(name = "content_item_id", nullable = false)
    private UUID contentItemId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnlockSource source;

    @Column(name = "unlocked_at", nullable = false)
    private Instant unlockedAt;

    public static ContentUnlock of(UUID contentItemId, UUID userId, UnlockSource source, Instant now) {
        ContentUnlock unlock = new ContentUnlock();
        unlock.contentItemId = contentItemId;
        unlock.userId = userId;
        unlock.source = source;
        unlock.unlockedAt = now;
        return unlock;
    }
}
