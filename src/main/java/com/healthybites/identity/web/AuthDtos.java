package com.healthybites.identity.web;

import com.healthybites.identity.UserAccount;
import com.healthybites.identity.app.SessionService.Session;
import com.healthybites.shared.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Cuerpos de los endpoints de sesión ({@code /auth/*}). */
final class AuthDtos {

    private AuthDtos() {}

    record LoginRequest(
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un correo válido") @Size(max = 254) String email,

            @NotBlank(message = "La contraseña es obligatoria") @Size(max = 128) String password) {}

    record ForgotPasswordRequest(
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un correo válido") @Size(max = 254) String email) {}

    // Misma política que el registro: bcrypt solo usa los primeros 72 bytes.
    record ResetPasswordRequest(
            @NotBlank(message = "El enlace no es válido") @Size(max = 128, message = "El enlace no es válido") String token,

            @NotBlank(message = "La nueva contraseña es obligatoria") @Size(min = 10, max = 64, message = "La contraseña debe tener entre 10 y 64 caracteres") String password) {}

    /** Sesión abierta: el access token va en el cuerpo (el web-app lo guarda solo en memoria). */
    record SessionResponse(String accessToken, String tokenType, long expiresIn, SessionUser user) {

        static SessionResponse of(Session session) {
            return new SessionResponse(
                    session.accessToken().value(),
                    "Bearer",
                    session.accessToken().expiresInSeconds(),
                    SessionUser.of(session.user()));
        }
    }

    record SessionUser(UUID id, String email, String firstName, String lastName, Role role) {

        static SessionUser of(UserAccount account) {
            return new SessionUser(
                    account.id(), account.email(), account.firstName(), account.lastName(), account.role());
        }
    }
}
