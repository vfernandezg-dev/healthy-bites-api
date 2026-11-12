package com.healthybites.community.app;

import com.healthybites.community.domain.Comment;
import com.healthybites.community.domain.CommentRepository;
import com.healthybites.community.domain.CommunityGroup;
import com.healthybites.community.domain.ModerationReport;
import com.healthybites.community.domain.ModerationReportRepository;
import com.healthybites.community.domain.Post;
import com.healthybites.community.domain.PostRepository;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.media.MediaFiles;
import com.healthybites.media.MediaPurpose;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publicaciones, comentarios y denuncias. Leer: grupo público o miembro activo. Publicar y comentar: miembro activo.
 * Editar: el autor. Borrar: el autor o el creador del grupo. Lo oculto por moderación no aparece.
 */
@Service
public class PostService {

    private final PostRepository posts;
    private final CommentRepository comments;
    private final ModerationReportRepository reports;
    private final GroupService groups;
    private final UserAccounts accounts;
    private final MediaFiles media;
    private final Clock clock;

    public PostService(
            PostRepository posts,
            CommentRepository comments,
            ModerationReportRepository reports,
            GroupService groups,
            UserAccounts accounts,
            MediaFiles media,
            Clock clock) {
        this.posts = posts;
        this.comments = comments;
        this.reports = reports;
        this.groups = groups;
        this.accounts = accounts;
        this.media = media;
        this.clock = clock;
    }

    public record PostCard(
            Post post,
            @Nullable UserAccount author,
            @Nullable String imagePath) {}

    public record CommentCard(Comment comment, @Nullable UserAccount author) {}

    public record PostData(
            String title, String body, @Nullable UUID imageMediaId) {}

    @Transactional(readOnly = true)
    public Page<PostCard> posts(UUID groupId, UUID userId, Pageable pageable) {
        CommunityGroup group = groups.find(groupId);
        groups.requireReader(group, userId);
        Page<Post> page = posts.findByGroupIdAndHiddenAtIsNull(groupId, pageable);
        Map<UUID, UserAccount> authors = accounts.findAllById(
                page.getContent().stream().map(Post::getAuthorId).distinct().toList());
        return page.map(post -> card(post, authors.get(post.getAuthorId())));
    }

    @Transactional(readOnly = true)
    public PostCard get(UUID postId, UUID userId) {
        Post post = visible(postId);
        groups.requireReader(groups.find(post.getGroupId()), userId);
        return card(post, accounts.findById(post.getAuthorId()).orElse(null));
    }

    @Transactional
    public PostCard create(UUID groupId, UUID userId, PostData data) {
        groups.requireActiveMember(groups.find(groupId), userId);
        checkImage(userId, data.imageMediaId());
        Post post = posts.save(Post.create(groupId, userId, data.title(), data.body(), data.imageMediaId()));
        return card(post, accounts.findById(userId).orElse(null));
    }

    @Transactional
    public PostCard edit(UUID postId, UUID userId, PostData data) {
        Post post = visible(postId);
        if (!post.getAuthorId().equals(userId)) {
            throw ApiException.forbidden("not-author", "Solo el autor puede editar la publicación");
        }
        checkImage(userId, data.imageMediaId());
        post.edit(data.title(), data.body(), data.imageMediaId());
        return card(post, accounts.findById(userId).orElse(null));
    }

    @Transactional
    public void delete(UUID postId, UUID userId) {
        Post post = visible(postId);
        if (!post.getAuthorId().equals(userId)
                && !groups.find(post.getGroupId()).isOwnedBy(userId)) {
            throw ApiException.forbidden("not-author", "Solo el autor o el creador del grupo pueden borrarla");
        }
        posts.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<CommentCard> comments(UUID postId, UUID userId, Pageable pageable) {
        Post post = visible(postId);
        groups.requireReader(groups.find(post.getGroupId()), userId);
        Page<Comment> page = comments.findByPostIdAndHiddenAtIsNull(postId, pageable);
        Map<UUID, UserAccount> authors = accounts.findAllById(
                page.getContent().stream().map(Comment::getAuthorId).distinct().toList());
        return page.map(c -> new CommentCard(c, authors.get(c.getAuthorId())));
    }

    @Transactional
    public CommentCard comment(UUID postId, UUID userId, String body) {
        Post post = visible(postId);
        groups.requireActiveMember(groups.find(post.getGroupId()), userId);
        Comment comment = comments.save(Comment.create(postId, userId, body));
        return new CommentCard(comment, accounts.findById(userId).orElse(null));
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = comments.findByIdAndHiddenAtIsNull(commentId)
                .orElseThrow(() -> ApiException.notFound("comment-not-found", "El comentario no existe"));
        Post post = visible(comment.getPostId());
        if (!comment.getAuthorId().equals(userId)
                && !groups.find(post.getGroupId()).isOwnedBy(userId)) {
            throw ApiException.forbidden("not-author", "Solo el autor o el creador del grupo pueden borrarlo");
        }
        comments.delete(comment);
    }

    /** Denuncia una publicación visible. Una denuncia abierta por persona y publicación. */
    @Transactional
    public void reportPost(UUID postId, UUID userId, String reason) {
        Post post = visible(postId);
        groups.requireReader(groups.find(post.getGroupId()), userId);
        if (reports.existsByReporterIdAndPostIdAndStatus(userId, postId, ModerationReport.Status.OPEN)) {
            throw ApiException.conflict("already-reported", "Ya denunciaste esta publicación");
        }
        reports.save(ModerationReport.ofPost(userId, postId, reason, clock.instant()));
    }

    @Transactional
    public void reportComment(UUID commentId, UUID userId, String reason) {
        Comment comment = comments.findByIdAndHiddenAtIsNull(commentId)
                .orElseThrow(() -> ApiException.notFound("comment-not-found", "El comentario no existe"));
        groups.requireReader(groups.find(visible(comment.getPostId()).getGroupId()), userId);
        if (reports.existsByReporterIdAndCommentIdAndStatus(userId, commentId, ModerationReport.Status.OPEN)) {
            throw ApiException.conflict("already-reported", "Ya denunciaste este comentario");
        }
        reports.save(ModerationReport.ofComment(userId, commentId, reason, clock.instant()));
    }

    private Post visible(UUID postId) {
        return posts.findByIdAndHiddenAtIsNull(postId)
                .orElseThrow(() -> ApiException.notFound("post-not-found", "La publicación no existe"));
    }

    private void checkImage(UUID userId, @Nullable UUID imageMediaId) {
        if (imageMediaId != null) {
            media.requireOwned(imageMediaId, userId, MediaPurpose.POST_IMAGE);
        }
    }

    private PostCard card(Post post, @Nullable UserAccount author) {
        UUID image = post.getImageMediaId();
        return new PostCard(post, author, image == null ? null : media.publicPath(image));
    }
}
