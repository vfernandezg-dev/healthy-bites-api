package com.healthybites.content.domain;

import com.healthybites.billing.Tier;
import com.healthybites.content.domain.ContentEnums.ContentCategory;
import com.healthybites.content.domain.ContentEnums.ContentStatus;
import com.healthybites.content.domain.ContentEnums.ContentType;
import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Un contenido. El slug se genera a partir del título al crearlo y no cambia después, para que los enlaces
 * compartidos sigan funcionando.
 */
@Entity
@Table(name = "content_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentItem extends BaseEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(columnDefinition = "text")
    private @Nullable String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_tier", nullable = false, length = 10)
    private Tier requiredTier;

    @Column(name = "cover_media_id")
    private @Nullable UUID coverMediaId;

    @Column(name = "file_media_id")
    private @Nullable UUID fileMediaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @Column(name = "published_at")
    private @Nullable Instant publishedAt;

    public record Data(
            String title,
            String summary,
            @Nullable String body,
            ContentType type,
            ContentCategory category,
            Tier requiredTier,
            @Nullable UUID coverMediaId,
            @Nullable UUID fileMediaId) {}

    public static ContentItem draft(UUID authorId, String slug, Data data) {
        ContentItem item = new ContentItem();
        item.authorId = authorId;
        item.slug = slug;
        item.status = ContentStatus.DRAFT;
        item.update(data);
        return item;
    }

    public void update(Data data) {
        this.title = data.title().trim();
        this.summary = data.summary().trim();
        this.body = data.body() == null || data.body().isBlank()
                ? null
                : data.body().trim();
        this.type = data.type();
        this.category = data.category();
        this.requiredTier = data.requiredTier();
        this.coverMediaId = data.coverMediaId();
        this.fileMediaId = data.fileMediaId();
    }

    public void publish(Instant now) {
        this.status = ContentStatus.PUBLISHED;
        if (publishedAt == null) {
            this.publishedAt = now;
        }
    }

    public void archive() {
        this.status = ContentStatus.ARCHIVED;
    }

    public boolean isPublished() {
        return status == ContentStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return status == ContentStatus.DRAFT;
    }
}
