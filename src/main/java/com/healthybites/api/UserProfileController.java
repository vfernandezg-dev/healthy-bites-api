package com.healthybites.api;

import com.healthybites.dto.UserProfileDTO;
import com.healthybites.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user/profile")
@PreAuthorize("hasAnyRole('CLIENTE','NUTRICIONISTA')")
public class UserProfileController {

    private final UsuarioService usuarioService;

    // Actualizar el perfil de un usuario
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDTO> updateProfile(@PathVariable Integer id, @Valid @RequestBody UserProfileDTO profileDTO) {
        UserProfileDTO updatedProfile = usuarioService.updateProfile(id, profileDTO);
        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }

    // Obtener el perfil de un usuario por su id
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Integer id) {
        UserProfileDTO userProfileDTO = usuarioService.getUserProfileById(id);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
    }

}
