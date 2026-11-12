package com.healthybites.profiles.web;

import com.healthybites.profiles.app.ProfileService;
import com.healthybites.profiles.web.ProfileDtos.MeResponse;
import com.healthybites.profiles.web.ProfileDtos.UpdateProfileRequest;
import com.healthybites.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@Tag(name = "Mi cuenta")
class MeController {

    private final ProfileService profiles;
    private final ProfileMapper mapper;

    MeController(ProfileService profiles, ProfileMapper mapper) {
        this.profiles = profiles;
        this.mapper = mapper;
    }

    @Operation(summary = "Usuario actual con el perfil de su rol")
    @GetMapping
    MeResponse me() {
        return mapper.toResponse(profiles.me(CurrentUser.id()));
    }

    @Operation(summary = "Actualiza el nombre y los datos del perfil")
    @PutMapping("/profile")
    MeResponse update(@Valid @RequestBody UpdateProfileRequest body) {
        return mapper.toResponse(profiles.update(
                CurrentUser.id(), body.firstName(), body.lastName(), mapper.toDataOrEmpty(body.client()), body.bio()));
    }
}
