package com.healthybites.community.app;

import com.healthybites.community.domain.Comment;
import com.healthybites.community.domain.CommentRepository;
import com.healthybites.community.domain.ModerationReport;
import com.healthybites.community.domain.ModerationReportRepository;
import com.healthybites.community.domain.Post;
import com.healthybites.community.domain.PostRepository;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Moderación (ADMIN): revisar denuncias y ocultar lo que incumple las normas, o descartarlas. */
@Service
public class ModerationService {

    private final ModerationReportRepository reports;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final UserAccounts accounts;
    private final Clock clock;

    public ModerationService(
            ModerationReportRepository reports,
            PostRepository posts,
            CommentRepository comments,
            UserAccounts accounts,
            Clock clock) {
        this.reports = reports;
        this.posts = posts;
        this.comments = comments;
        this.accounts = accounts;
        this.clock = clock;
    }

    public enum Action {
        HIDE,
        DISMISS
    }

    public enum TargetType {
        POST,
        COMMENT
    }

    /** Una denuncia con un extracto de lo denunciado y su autor. */
    public record ReportCard(
            ModerationReport report,
            TargetType targetType,
            UUID targetId,
            String excerpt,
            boolean targetHidden,
            @Nullable UserAccount targetAuthor,
            @Nullable UserAccount reporter) {}

    @Transactional(readOnly = true)
    public Page<ReportCard> reports(ModerationReport.Status status, Pageable pageable) {
        return reports.findByStatus(status, pageable).map(this::card);
    }

    /** Ocultar cierra todas las denuncias abiertas de lo mismo; descartar solo esta. */
    @Transactional
    public ReportCard resolve(UUID reportId, UUID moderatorId, Action action, String note) {
        ModerationReport report = reports.findById(reportId)
                .orElseThrow(() -> ApiException.notFound("report-not-found", "La denuncia no existe"));
        if (report.getStatus() != ModerationReport.Status.OPEN) {
            throw ApiException.conflict("report-closed", "La denuncia ya está resuelta");
        }
        Instant now = clock.instant();
        if (action == Action.DISMISS) {
            report.close(ModerationReport.Status.DISMISSED, moderatorId, now);
            return card(report);
        }
        if (report.getPostId() != null) {
            posts.findById(report.getPostId()).ifPresent(post -> post.hide(moderatorId, note, now));
            reports.findByPostIdAndStatus(report.getPostId(), ModerationReport.Status.OPEN)
                    .forEach(r -> r.close(ModerationReport.Status.RESOLVED, moderatorId, now));
        } else if (report.getCommentId() != null) {
            comments.findById(report.getCommentId()).ifPresent(comment -> comment.hide(moderatorId, note, now));
            reports.findByCommentIdAndStatus(report.getCommentId(), ModerationReport.Status.OPEN)
                    .forEach(r -> r.close(ModerationReport.Status.RESOLVED, moderatorId, now));
        }
        return card(report);
    }

    private ReportCard card(ModerationReport report) {
        UserAccount reporter = accounts.findById(report.getReporterId()).orElse(null);
        if (report.getPostId() != null) {
            Post post = posts.findById(report.getPostId()).orElseThrow();
            return new ReportCard(
                    report,
                    TargetType.POST,
                    post.requireId(),
                    excerpt(post.getTitle() + " — " + post.getBody()),
                    post.isHidden(),
                    accounts.findById(post.getAuthorId()).orElse(null),
                    reporter);
        }
        Comment comment =
                comments.findById(Objects.requireNonNull(report.getCommentId())).orElseThrow();
        return new ReportCard(
                report,
                TargetType.COMMENT,
                comment.requireId(),
                excerpt(comment.getBody()),
                comment.isHidden(),
                accounts.findById(comment.getAuthorId()).orElse(null),
                reporter);
    }

    private static String excerpt(String text) {
        return text.length() <= 200 ? text : text.substring(0, 197) + "...";
    }
}
