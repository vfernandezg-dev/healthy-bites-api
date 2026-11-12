package com.healthybites.identity.web;

import com.healthybites.identity.app.PasswordChangeService;
import com.healthybites.identity.app.SessionService.Session;
import com.healthybites.identity.web.AdminDtos.ChangePasswordRequest;
import com.healthybites.identity.web.AuthDtos.SessionResponse;
import com.healthybites.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@Tag(name = "Mi cuenta")
class AccountController {

    private final PasswordChangeService passwordChange;
    private final RefreshTokenCookies refreshCookies;

    AccountController(PasswordChangeService passwordChange, RefreshTokenCookies refreshCookies) {
        this.passwordChange = passwordChange;
        this.refreshCookies = refreshCookies;
    }

    @Operation(
            summary = "Cambia la contraseña",
            description = "Cierra todas las sesiones (también en otros dispositivos) y devuelve una nueva.")
    @PutMapping("/password")
    ResponseEntity<SessionResponse> changePassword(@Valid @RequestBody ChangePasswordRequest body) {
        Session session = passwordChange.change(CurrentUser.id(), body.currentPassword(), body.newPassword());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookies.create(
                                session.refreshToken().value(),
                                session.refreshToken().expiresAt()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(SessionResponse.of(session));
    }
}
