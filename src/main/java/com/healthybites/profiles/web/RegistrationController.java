package com.healthybites.profiles.web;

import com.healthybites.identity.Registrations.InvitationPreview;
import com.healthybites.profiles.app.ProfileService;
import com.healthybites.profiles.web.ProfileDtos.AcceptInvitationRequest;
import com.healthybites.profiles.web.ProfileDtos.InvitationPreviewRequest;
import com.healthybites.profiles.web.ProfileDtos.InvitationPreviewResponse;
import com.healthybites.profiles.web.ProfileDtos.MeResponse;
import com.healthybites.profiles.web.ProfileDtos.RegisterRequest;
import com.healthybites.shared.web.Locations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Registro", description = "Alta de clientes y de nutricionistas invitados")
@SecurityRequirements
class RegistrationController {

    private final ProfileService profiles;
    private final ProfileMapper mapper;

    RegistrationController(ProfileService profiles, ProfileMapper mapper) {
        this.profiles = profiles;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Registro público de clientes",
            description =
                    "Crea la cuenta y su perfil. Para entrar, el web-app inicia sesión con las mismas credenciales.")
    @PostMapping("/register")
    ResponseEntity<MeResponse> register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request) {
        MeResponse me = mapper.toResponse(profiles.registerClient(
                body.email(),
                body.password(),
                body.firstName(),
                body.lastName(),
                mapper.toDataOrEmpty(body.profile()),
                request.getRemoteAddr()));
        return ResponseEntity.created(Locations.of("/me")).body(me);
    }

    @Operation(summary = "Datos de una invitación de nutricionista (para mostrarlos antes de aceptar)")
    @PostMapping("/invitations/preview")
    InvitationPreviewResponse previewInvitation(@Valid @RequestBody InvitationPreviewRequest body) {
        InvitationPreview preview = profiles.previewInvitation(body.token());
        return new InvitationPreviewResponse(preview.email(), preview.firstName(), preview.lastName());
    }

    @Operation(summary = "Acepta la invitación: crea la cuenta de nutricionista con su contraseña")
    @PostMapping("/invitations/accept")
    ResponseEntity<MeResponse> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest body) {
        MeResponse me = mapper.toResponse(profiles.acceptInvitation(body.token(), body.password(), body.bio()));
        return ResponseEntity.created(Locations.of("/me")).body(me);
    }
}
