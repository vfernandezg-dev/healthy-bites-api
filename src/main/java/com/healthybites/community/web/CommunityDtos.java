package com.healthybites.community.web;

import com.healthybites.community.app.GroupService;
import com.healthybites.community.app.GroupService.GroupCard;
import com.healthybites.community.app.ModerationService;
import com.healthybites.community.app.ModerationService.ReportCard;
import com.healthybites.community.app.PostService;
import com.healthybites.community.app.PostService.CommentCard;
import com.healthybites.community.app.PostService.PostCard;
import com.healthybites.community.domain.CommunityGroup;
import com.healthybites.community.domain.GroupMembership;
import com.healthybites.community.domain.ModerationReport;
import com.healthybites.identity.PersonResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class CommunityDtos {

    private CommunityDtos() {}

    record MembershipResponse(GroupMembership.Role role, GroupMembership.Status status) {

        static @Nullable MembershipResponse of(@Nullable GroupMembership membership) {
            return membership == null ? null : new MembershipResponse(membership.getRole(), membership.getStatus());
        }
    }

    /** Un grupo del directorio con sus miembros activos y mi membresía (null si no pertenezco). */
    record GroupResponse(
            UUID id,
            String name,
            @Nullable String description,
            CommunityGroup.Visibility visibility,
            long members,
            @Nullable MembershipResponse membership,
            @Nullable Instant createdAt) {

        static GroupResponse of(GroupCard card) {
            CommunityGroup group = card.group();
            return new GroupResponse(
                    group.requireId(),
                    group.getName(),
                    group.getDescription(),
                    group.getVisibility(),
                    card.members(),
                    MembershipResponse.of(card.membership()),
                    group.getCreatedAt());
        }
    }

    record GroupRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 80, message = "Máximo 80 caracteres") String name,

            @Size(max = 500, message = "Máximo 500 caracteres") @Nullable String description,

            @NotNull(message = "Elige si es público o privado") CommunityGroup.Visibility visibility) {

        GroupService.GroupData toData() {
            return new GroupService.GroupData(name, description, visibility);
        }
    }

    record MemberResponse(
            @Nullable PersonResponse person,
            GroupMembership.Role role,
            GroupMembership.Status status,
            @Nullable Instant since) {

        static MemberResponse of(GroupService.Member member) {
            return new MemberResponse(
                    PersonResponse.of(member.account()),
                    member.membership().getRole(),
                    member.membership().getStatus(),
                    member.membership().getCreatedAt());
        }
    }

    record PostResponse(
            UUID id,
            UUID groupId,
            String title,
            String body,
            @Nullable String imagePath,
            @Nullable PersonResponse author,
            @Nullable Instant createdAt,
            @Nullable Instant updatedAt) {

        static PostResponse of(PostCard card) {
            return new PostResponse(
                    card.post().requireId(),
                    card.post().getGroupId(),
                    card.post().getTitle(),
                    card.post().getBody(),
                    card.imagePath(),
                    PersonResponse.of(card.author()),
                    card.post().getCreatedAt(),
                    card.post().getUpdatedAt());
        }
    }

    record PostRequest(
            @NotBlank(message = "El título es obligatorio") @Size(max = 120, message = "Máximo 120 caracteres") String title,

            @NotBlank(message = "Escribe algo") @Size(max = 4000, message = "Máximo 4000 caracteres") String body,

            @Nullable UUID imageMediaId) {

        PostService.PostData toData() {
            return new PostService.PostData(title, body, imageMediaId);
        }
    }

    record CommentResponse(
            UUID id,
            String body,
            @Nullable PersonResponse author,
            @Nullable Instant createdAt) {

        static CommentResponse of(CommentCard card) {
            return new CommentResponse(
                    card.comment().requireId(),
                    card.comment().getBody(),
                    PersonResponse.of(card.author()),
                    card.comment().getCreatedAt());
        }
    }

    record CommentRequest(
            @NotBlank(message = "Escribe algo") @Size(max = 1000, message = "Máximo 1000 caracteres") String body) {}

    record ReportRequest(
            @NotBlank(message = "Cuéntanos el motivo") @Size(max = 500, message = "Máximo 500 caracteres") String reason) {}

    record ReportResponse(
            UUID id,
            ModerationService.TargetType targetType,
            UUID targetId,
            String excerpt,
            boolean targetHidden,
            @Nullable PersonResponse targetAuthor,
            @Nullable PersonResponse reporter,
            String reason,
            ModerationReport.Status status,
            Instant createdAt,
            @Nullable Instant resolvedAt) {

        static ReportResponse of(ReportCard card) {
            ModerationReport report = card.report();
            return new ReportResponse(
                    report.requireId(),
                    card.targetType(),
                    card.targetId(),
                    card.excerpt(),
                    card.targetHidden(),
                    PersonResponse.of(card.targetAuthor()),
                    PersonResponse.of(card.reporter()),
                    report.getReason(),
                    report.getStatus(),
                    report.getCreatedAt(),
                    report.getResolvedAt());
        }
    }

    record ResolveRequest(
            @NotNull(message = "Elige una acción") ModerationService.Action action,

            @NotBlank(message = "Explica la decisión") @Size(max = 200, message = "Máximo 200 caracteres") String note) {}
}
