package com.healthybites.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Base de lo que publica un nutricionista (contenidos y planes): nace en borrador, se publica y se retira. Un
 * borrador solo lo ve su autor; lo retirado deja de ofrecerse, pero quien ya lo tenía lo conserva.
 *
 * <p>Las columnas {@code status} y {@code published_at} son las mismas en todas las tablas que la usan.
 */
@MappedSuperclass
@Getter
public abstract class PublishableEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "published_at")
    private @Nullable Instant publishedAt;

    /** El nutricionista que lo creó. */
    public abstract UUID getAuthorId();

    /** Lo publica; la fecha de publicación es la de la primera vez. */
    public void publish(Instant now) {
        this.status = PublicationStatus.PUBLISHED;
        if (publishedAt == null) {
            this.publishedAt = now;
        }
    }

    public void archive() {
        this.status = PublicationStatus.ARCHIVED;
    }

    public boolean isPublished() {
        return status == PublicationStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return status == PublicationStatus.DRAFT;
    }

    /** Lo publicado lo ve cualquiera; lo demás, solo su autor (si entra como nutricionista o admin). */
    public boolean isVisibleTo(UUID readerId, boolean staff) {
        return isPublished() || (staff && getAuthorId().equals(readerId));
    }
}
