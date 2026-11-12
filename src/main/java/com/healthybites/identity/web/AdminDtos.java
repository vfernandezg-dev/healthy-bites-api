package com.healthybites.identity.web;

import com.healthybites.identity.app.AdminUserService;
import com.healthybites.identity.app.InvitationService.InvitationView;
import com.healthybites.identity.domain.NutritionistInvitation;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserStatus;
import com.healthybites.shared.security.Role;
import com.healthybites.shared.validation.EmailAddress;
import com.healthybites.shared.validation.Password;
import com.healthybites.shared.validation.PersonName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Cuerpos del panel de administración de cuentas e invitaciones. */
final class AdminDtos {

    private AdminDtos() {}

    record AdminUserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            Role role,
            UserStatus status,
            @Nullable Instant createdAt,
            @Nullable Instant lastLoginAt) {

        static AdminUserResponse of(User user) {
            return new AdminUserResponse(
                    user.requireId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getStatus(),
                    user.getCreatedAt(),
                    user.getLastLoginAt());
        }
    }

    record UserSummaryResponse(long total, long active, long deactivated, Map<Role, Long> byRole, long newLast30Days) {

        static UserSummaryResponse of(AdminUserService.Summary summary) {
            return new UserSummaryResponse(
                    summary.total(),
                    summary.active(),
                    summary.deactivated(),
                    summary.byRole(),
                    summary.newLast30Days());
        }
    }

    record InviteNutritionistRequest(
            @EmailAddress String email,

            @NotBlank(message = "El nombre es obligatorio") @PersonName
            String firstName,

            @NotBlank(message = "El apellido es obligatorio") @PersonName
            String lastName) {}

    record InvitationResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            NutritionistInvitation.Status status,
            Instant expiresAt,
            @Nullable Instant acceptedAt,
            @Nullable Instant createdAt) {

        static InvitationResponse of(InvitationView view) {
            NutritionistInvitation invitation = view.invitation();
            return new InvitationResponse(
                    invitation.requireId(),
                    invitation.getEmail(),
                    invitation.getFirstName(),
                    invitation.getLastName(),
                    view.status(),
                    invitation.getExpiresAt(),
                    invitation.getAcceptedAt(),
                    invitation.getCreatedAt());
        }
    }

    record ChangePasswordRequest(
            @NotBlank(message = "La contraseña actual es obligatoria") @Size(max = 128) String currentPassword,

            @NotBlank(message = "La nueva contraseña es obligatoria") @Password
            String newPassword) {}
}
