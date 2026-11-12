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

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(name = "hidden_at")
    private @Nullable Instant hiddenAt;

    @Column(name = "hidden_by")
    private @Nullable UUID hiddenBy;

    @Column(name = "hidden_reason", length = 200)
    private @Nullable String hiddenReason;

    public static Comment create(UUID postId, UUID authorId, String body) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.authorId = authorId;
        comment.body = body.trim();
        return comment;
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
