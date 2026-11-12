package com.healthybites.identity.web;

import com.healthybites.identity.app.InvalidRefreshTokenException;
import com.healthybites.identity.app.PasswordResetService;
import com.healthybites.identity.app.SessionService;
import com.healthybites.identity.app.SessionService.Session;
import com.healthybites.identity.web.AuthDtos.ForgotPasswordRequest;
import com.healthybites.identity.web.AuthDtos.LoginRequest;
import com.healthybites.identity.web.AuthDtos.ResetPasswordRequest;
import com.healthybites.identity.web.AuthDtos.SessionResponse;
import com.healthybites.shared.web.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Sesión", description = "Login, renovación, logout y restablecimiento de contraseña")
@SecurityRequirements // públicos: no llevan bearer
class AuthController {

    private static final String SESSION_EXPIRED = "Tu sesión ha caducado. Vuelve a iniciar sesión.";

    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookies refreshCookies;
    private final CookieRequestGuard cookieRequestGuard;

    AuthController(
            SessionService sessionService,
            PasswordResetService passwordResetService,
            RefreshTokenCookies refreshCookies,
            CookieRequestGuard cookieRequestGuard) {
        this.sessionService = sessionService;
        this.passwordResetService = passwordResetService;
        this.refreshCookies = refreshCookies;
        this.cookieRequestGuard = cookieRequestGuard;
    }

    @Operation(summary = "Abre una sesión: access token en el cuerpo y refresh token en una cookie httpOnly")
    @PostMapping("/login")
    ResponseEntity<SessionResponse> login(@Valid @RequestBody LoginRequest login, HttpServletRequest request) {
        return withSession(sessionService.login(login.email(), login.password(), request.getRemoteAddr()));
    }

    @Operation(summary = "Renueva la sesión con la cookie del refresh token, que se rota en cada uso")
    @PostMapping("/refresh")
    ResponseEntity<?> refresh(HttpServletRequest request) {
        cookieRequestGuard.check(request);
        try {
            return withSession(sessionService.refresh(refreshCookies.read(request), request.getRemoteAddr()));
        } catch (InvalidRefreshTokenException e) {
            log.debug("Refresh rechazado: {}", e.getMessage());
            ProblemDetail problem =
                    GlobalExceptionHandler.problem(HttpStatus.UNAUTHORIZED, "session-expired", SESSION_EXPIRED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, refreshCookies.clear())
                    .body(problem);
        }
    }

    @Operation(summary = "Cierra la sesión: revoca el refresh token y borra la cookie")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        cookieRequestGuard.check(request);
        sessionService.logout(refreshCookies.read(request));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.clear())
                .build();
    }

    @Operation(
            summary = "Pide un enlace para restablecer la contraseña",
            description = "Siempre 202, exista o no la cuenta: la respuesta no revela qué correos están registrados.")
    @PostMapping("/forgot-password")
    ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body, HttpServletRequest request) {
        passwordResetService.requestReset(body.email(), request.getRemoteAddr());
        return ResponseEntity.accepted()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @Operation(summary = "Cambia la contraseña con el token del enlace y cierra todas las sesiones del usuario")
    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest body) {
        passwordResetService.resetPassword(body.token(), body.password());
        return ResponseEntity.noContent()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    private ResponseEntity<SessionResponse> withSession(Session session) {
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
