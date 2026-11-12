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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        Page<ModerationReport> page = reports.findByStatus(status, pageable);
        return new PageImpl<>(cards(page.getContent()), pageable, page.getTotalElements());
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
        return cards(List.of(report)).getFirst();
    }

    /** Las denuncias con lo denunciado y las personas cargados en lote: cuatro consultas por página, no tres por fila. */
    private List<ReportCard> cards(List<ModerationReport> page) {
        Map<UUID, Post> postsById = posts
                .findAllById(page.stream()
                        .map(ModerationReport::getPostId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Post::requireId, Function.identity()));
        Map<UUID, Comment> commentsById = comments
                .findAllById(page.stream()
                        .map(ModerationReport::getCommentId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Comment::requireId, Function.identity()));
        Set<UUID> people = new HashSet<>();
        page.forEach(report -> people.add(report.getReporterId()));
        postsById.values().forEach(post -> people.add(post.getAuthorId()));
        commentsById.values().forEach(comment -> people.add(comment.getAuthorId()));
        Map<UUID, UserAccount> accountsById = accounts.findAllById(people);

        return page.stream()
                .map(report -> {
                    UserAccount reporter = accountsById.get(report.getReporterId());
                    if (report.getPostId() != null) {
                        Post post = Objects.requireNonNull(postsById.get(report.getPostId()));
                        return new ReportCard(
                                report,
                                TargetType.POST,
                                post.requireId(),
                                excerpt(post.getTitle() + " — " + post.getBody()),
                                post.isHidden(),
                                accountsById.get(post.getAuthorId()),
                                reporter);
                    }
                    Comment comment =
                            Objects.requireNonNull(commentsById.get(Objects.requireNonNull(report.getCommentId())));
                    return new ReportCard(
                            report,
                            TargetType.COMMENT,
                            comment.requireId(),
                            excerpt(comment.getBody()),
                            comment.isHidden(),
                            accountsById.get(comment.getAuthorId()),
                            reporter);
                })
                .toList();
    }

    private static String excerpt(String text) {
        return text.length() <= 200 ? text : text.substring(0, 197) + "...";
    }
}
