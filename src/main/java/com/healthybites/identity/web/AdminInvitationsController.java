package com.healthybites.identity.web;

import com.healthybites.identity.app.InvitationService;
import com.healthybites.identity.web.AdminDtos.InvitationResponse;
import com.healthybites.identity.web.AdminDtos.InviteNutritionistRequest;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Locations;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/nutritionist-invitations")
@Tag(name = "Admin · Invitaciones", description = "Alta de nutricionistas por invitación (solo ADMIN)")
class AdminInvitationsController {

    private static final Set<String> SORTABLE = Set.of("createdAt", "email", "expiresAt");

    private final InvitationService invitations;

    AdminInvitationsController(InvitationService invitations) {
        this.invitations = invitations;
    }

    @Operation(summary = "Invita a un nutricionista: recibe un enlace por correo para crear su cuenta")
    @PostMapping
    ResponseEntity<InvitationResponse> invite(@Valid @RequestBody InviteNutritionistRequest body) {
        InvitationResponse created = InvitationResponse.of(
                invitations.invite(body.email(), body.firstName(), body.lastName(), CurrentUser.id()));
        return ResponseEntity.created(Locations.of("/admin/nutritionist-invitations/{id}", created.id()))
                .body(created);
    }

    @Operation(summary = "Lista las invitaciones con su estado")
    @GetMapping
    PagedModel<InvitationResponse> list(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return new PagedModel<>(
                invitations.list(Sorting.restrict(pageable, SORTABLE)).map(InvitationResponse::of));
    }

    @Operation(summary = "Reenvía la invitación con un enlace nuevo (el anterior deja de valer)")
    @PostMapping("/{id}/resend")
    InvitationResponse resend(@PathVariable UUID id) {
        return InvitationResponse.of(invitations.resend(id));
    }

    @Operation(summary = "Revoca una invitación pendiente")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> revoke(@PathVariable UUID id) {
        invitations.revoke(id);
        return ResponseEntity.noContent().build();
    }
}
