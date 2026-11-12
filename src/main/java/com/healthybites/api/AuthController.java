package com.healthybites.api;

import com.healthybites.dto.AuthResponseDTO;
import com.healthybites.dto.LoginDTO;
import com.healthybites.dto.PasswordResetRequestDTO;
import com.healthybites.dto.ResetPasswordDTO;
import com.healthybites.dto.UserProfileDTO;
import com.healthybites.dto.UserRegistrationDTO;
import com.healthybites.security.session.CookieRequestGuard;
import com.healthybites.security.session.RefreshTokenCookies;
import com.healthybites.security.session.SessionService;
import com.healthybites.security.session.SessionService.Session;
import com.healthybites.security.token.InvalidRefreshTokenException;
import com.healthybites.service.AuthService;
import com.healthybites.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String SESSION_EXPIRED = "Tu sesión ha caducado. Vuelve a iniciar sesión.";

    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final SessionService sessionService;
    private final RefreshTokenCookies refreshCookies;
    private final CookieRequestGuard cookieRequestGuard;

    // Registro de un cliente
    @PostMapping("/register/cliente")
    public ResponseEntity<UserProfileDTO> registerClient(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        UserProfileDTO userProfileDTO = usuarioService.registrarCliente(userRegistrationDTO);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.CREATED);
    }

    // Registro de un nutricionista
    @PostMapping("/register/nutricionista")
    public ResponseEntity<UserProfileDTO> registerNutritionist(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        UserProfileDTO userProfileDTO = usuarioService.registrarNutricionista(userRegistrationDTO);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.CREATED);
    }

    /** Abre una sesión: access token en el cuerpo y refresh token en una cookie httpOnly. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        Session session = sessionService.login(loginDTO, request.getRemoteAddr());
        return withSession(session);
    }

    /** Renueva la sesión con la cookie del refresh token, que se rota en cada uso. */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        cookieRequestGuard.check(request);
        try {
            Session session = sessionService.refresh(refreshCookies.read(request), request.getRemoteAddr());
            return withSession(session);
        } catch (InvalidRefreshTokenException e) {
            log.debug("Refresh rechazado: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, SESSION_EXPIRED);
            problem.setProperty("message", SESSION_EXPIRED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, refreshCookies.clear())
                    .body(problem);
        }
    }

    /** Cierra la sesión: revoca el refresh token y borra la cookie. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        cookieRequestGuard.check(request);
        sessionService.logout(refreshCookies.read(request));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookies.clear()).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody PasswordResetRequestDTO requestDTO) throws Exception {
        authService.sendPasswordResetToken(requestDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        authService.resetPassword(resetPasswordDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<AuthResponseDTO> withSession(Session session) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        refreshCookies.create(session.refreshToken().value(), session.refreshToken().expiresAt()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(session.body());
    }
}
