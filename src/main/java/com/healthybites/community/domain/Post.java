package com.healthybites.community.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Publicación en un grupo. La moderación la oculta (no la borra): queda constancia de quién y por qué. */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 4000)
    private String body;

    @Column(name = "image_media_id")
    private @Nullable UUID imageMediaId;

    @Column(name = "hidden_at")
    private @Nullable Instant hiddenAt;

    @Column(name = "hidden_by")
    private @Nullable UUID hiddenBy;

    @Column(name = "hidden_reason", length = 200)
    private @Nullable String hiddenReason;

    public static Post create(UUID groupId, UUID authorId, String title, String body, @Nullable UUID imageMediaId) {
        Post post = new Post();
        post.groupId = groupId;
        post.authorId = authorId;
        post.edit(title, body, imageMediaId);
        return post;
    }

    public void edit(String title, String body, @Nullable UUID imageMediaId) {
        this.title = title.trim();
        this.body = body.trim();
        this.imageMediaId = imageMediaId;
    }

    public void hide(UUID moderatorId, String reason, Instant now) {
        this.hiddenAt = now;
        this.hiddenBy = moderatorId;
        this.hiddenReason = reason;
    }

    public boolean isHidden() {
        return hiddenAt != null;
    }
}
