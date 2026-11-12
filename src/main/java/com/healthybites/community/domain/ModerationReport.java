package com.healthybites.community.domain;

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

/** Denuncia de una publicación o de un comentario (exactamente uno de los dos). */
@Entity
@Table(name = "moderation_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModerationReport {

    public enum Status {
        OPEN,
        RESOLVED,
        DISMISSED
    }

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "post_id")
    private @Nullable UUID postId;

    @Column(name = "comment_id")
    private @Nullable UUID commentId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(name = "resolved_by")
    private @Nullable UUID resolvedBy;

    @Column(name = "resolved_at")
    private @Nullable Instant resolvedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static ModerationReport ofPost(UUID reporterId, UUID postId, String reason, Instant now) {
        ModerationReport report = open(reporterId, reason, now);
        report.postId = postId;
        return report;
    }

    public static ModerationReport ofComment(UUID reporterId, UUID commentId, String reason, Instant now) {
        ModerationReport report = open(reporterId, reason, now);
        report.commentId = commentId;
        return report;
    }

    private static ModerationReport open(UUID reporterId, String reason, Instant now) {
        ModerationReport report = new ModerationReport();
        report.reporterId = reporterId;
        report.reason = reason.trim();
        report.status = Status.OPEN;
        report.createdAt = now;
        return report;
    }

    public void close(Status outcome, UUID moderatorId, Instant now) {
        this.status = outcome;
        this.resolvedBy = moderatorId;
        this.resolvedAt = now;
    }

    public UUID requireId() {
        if (id == null) {
            throw new IllegalStateException("La denuncia aún no tiene id");
        }
        return id;
    }
}
