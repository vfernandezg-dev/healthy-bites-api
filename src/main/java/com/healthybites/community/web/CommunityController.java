package com.healthybites.community.web;

import com.healthybites.community.app.GroupService;
import com.healthybites.community.app.GroupService.GroupCard;
import com.healthybites.community.app.PostService;
import com.healthybites.community.app.PostService.CommentCard;
import com.healthybites.community.app.PostService.PostCard;
import com.healthybites.community.web.CommunityDtos.CommentRequest;
import com.healthybites.community.web.CommunityDtos.CommentResponse;
import com.healthybites.community.web.CommunityDtos.GroupRequest;
import com.healthybites.community.web.CommunityDtos.GroupResponse;
import com.healthybites.community.web.CommunityDtos.MemberResponse;
import com.healthybites.community.web.CommunityDtos.PostRequest;
import com.healthybites.community.web.CommunityDtos.PostResponse;
import com.healthybites.community.web.CommunityDtos.ReportRequest;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Locations;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Comunidad", description = "Grupos, membresías, publicaciones, comentarios y denuncias")
class CommunityController {

    private static final Set<String> GROUP_SORT = Set.of("createdAt", "name");
    private static final Set<String> POST_SORT = Set.of("createdAt");

    private final GroupService groups;
    private final PostService posts;

    CommunityController(GroupService groups, PostService posts) {
        this.groups = groups;
        this.posts = posts;
    }

    // ---------------------------------------------------------------- grupos

    @Operation(summary = "Directorio de grupos (públicos y privados) con mi membresía")
    @GetMapping("/groups")
    PagedModel<GroupResponse> directory(
            @RequestParam(required = false) @Nullable String q,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(groups.directory(CurrentUser.id(), q, Sorting.restrict(pageable, GROUP_SORT))
                .map(GroupResponse::of));
    }

    @PostMapping("/groups")
    ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest body) {
        GroupCard card = groups.create(CurrentUser.id(), body.toData());
        return ResponseEntity.created(Locations.of("/groups/{id}", card.group().requireId()))
                .body(GroupResponse.of(card));
    }

    @GetMapping("/groups/{id}")
    GroupResponse group(@PathVariable UUID id) {
        return GroupResponse.of(groups.get(id, CurrentUser.id()));
    }

    @PutMapping("/groups/{id}")
    GroupResponse update(@PathVariable UUID id, @Valid @RequestBody GroupRequest body) {
        return GroupResponse.of(groups.update(id, CurrentUser.id(), body.toData()));
    }

    @DeleteMapping("/groups/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        groups.delete(id, CurrentUser.id(), CurrentUser.hasRole("ADMIN"));
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- membresías

    @Operation(summary = "Entra en un grupo público o pide entrar en uno privado")
    @PostMapping("/groups/{id}/members")
    GroupResponse join(@PathVariable UUID id) {
        return GroupResponse.of(groups.join(id, CurrentUser.id()));
    }

    @DeleteMapping("/groups/{id}/members/me")
    ResponseEntity<Void> leave(@PathVariable UUID id) {
        groups.leave(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Miembros (el creador ve también las solicitudes pendientes)")
    @GetMapping("/groups/{id}/members")
    List<MemberResponse> members(@PathVariable UUID id) {
        return groups.members(id, CurrentUser.id()).stream()
                .map(MemberResponse::of)
                .toList();
    }

    @PostMapping("/groups/{id}/members/{userId}/approve")
    ResponseEntity<Void> approve(@PathVariable UUID id, @PathVariable UUID userId) {
        groups.approve(id, userId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rechaza una solicitud o expulsa a un miembro (solo el creador)")
    @DeleteMapping("/groups/{id}/members/{userId}")
    ResponseEntity<Void> remove(@PathVariable UUID id, @PathVariable UUID userId) {
        groups.remove(id, userId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- publicaciones y comentarios

    @GetMapping("/groups/{id}/posts")
    PagedModel<PostResponse> posts(
            @PathVariable UUID id,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(posts.posts(id, CurrentUser.id(), Sorting.restrict(pageable, POST_SORT))
                .map(PostResponse::of));
    }

    @PostMapping("/groups/{id}/posts")
    ResponseEntity<PostResponse> publish(@PathVariable UUID id, @Valid @RequestBody PostRequest body) {
        PostCard card = posts.create(id, CurrentUser.id(), body.toData());
        return ResponseEntity.created(Locations.of("/posts/{id}", card.post().requireId()))
                .body(PostResponse.of(card));
    }

    @GetMapping("/posts/{id}")
    PostResponse post(@PathVariable UUID id) {
        return PostResponse.of(posts.get(id, CurrentUser.id()));
    }

    @PutMapping("/posts/{id}")
    PostResponse edit(@PathVariable UUID id, @Valid @RequestBody PostRequest body) {
        return PostResponse.of(posts.edit(id, CurrentUser.id(), body.toData()));
    }

    @DeleteMapping("/posts/{id}")
    ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        posts.delete(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{id}/comments")
    List<CommentResponse> comments(@PathVariable UUID id) {
        return posts.comments(id, CurrentUser.id()).stream()
                .map(CommentResponse::of)
                .toList();
    }

    @PostMapping("/posts/{id}/comments")
    ResponseEntity<CommentResponse> comment(@PathVariable UUID id, @Valid @RequestBody CommentRequest body) {
        CommentCard card = posts.comment(id, CurrentUser.id(), body.body());
        return ResponseEntity.created(
                        Locations.of("/comments/{id}", card.comment().requireId()))
                .body(CommentResponse.of(card));
    }

    @DeleteMapping("/comments/{id}")
    ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        posts.deleteComment(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- denuncias

    @Operation(summary = "Denuncia una publicación (la revisa un ADMIN)")
    @PostMapping("/posts/{id}/reports")
    ResponseEntity<Void> reportPost(@PathVariable UUID id, @Valid @RequestBody ReportRequest body) {
        posts.reportPost(id, CurrentUser.id(), body.reason());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/comments/{id}/reports")
    ResponseEntity<Void> reportComment(@PathVariable UUID id, @Valid @RequestBody ReportRequest body) {
        posts.reportComment(id, CurrentUser.id(), body.reason());
        return ResponseEntity.accepted().build();
    }
}
