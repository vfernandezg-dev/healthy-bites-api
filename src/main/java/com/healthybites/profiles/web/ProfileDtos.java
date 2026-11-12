package com.healthybites.profiles.web;

import com.healthybites.identity.Role;
import com.healthybites.profiles.Sex;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Cuerpos del registro, de la invitación y de {@code /me}. */
final class ProfileDtos {

    private ProfileDtos() {}

    /** Datos físicos del cliente, todos opcionales. */
    record ClientData(
            @Nullable Sex sex,

            @Past(message = "La fecha de nacimiento debe ser anterior a hoy") @Nullable LocalDate birthDate,

            @DecimalMin(value = "50", message = "La altura mínima es 50 cm") @DecimalMax(value = "260", message = "La altura máxima es 260 cm") @Digits(integer = 3, fraction = 1, message = "Como mucho un decimal") @Nullable BigDecimal heightCm,

            @DecimalMin(value = "20", message = "El peso mínimo es 20 kg") @DecimalMax(value = "400", message = "El peso máximo es 400 kg") @Digits(integer = 3, fraction = 1, message = "Como mucho un decimal") @Nullable BigDecimal weightKg) {}

    record RegisterRequest(
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un correo válido") @Size(max = 254) String email,

            @NotBlank(message = "La contraseña es obligatoria") @Size(min = 10, max = 64, message = "La contraseña debe tener entre 10 y 64 caracteres") String password,

            @NotBlank(message = "El nombre es obligatorio") @Size(max = 60, message = "Máximo 60 caracteres") String firstName,

            @NotBlank(message = "El apellido es obligatorio") @Size(max = 60, message = "Máximo 60 caracteres") String lastName,

            @Valid @Nullable ClientData profile) {}

    record InvitationPreviewRequest(
            @NotBlank(message = "La invitación no es válida") @Size(max = 128) String token) {}

    record InvitationPreviewResponse(String email, String firstName, String lastName) {}

    record AcceptInvitationRequest(
            @NotBlank(message = "La invitación no es válida") @Size(max = 128) String token,

            @NotBlank(message = "La contraseña es obligatoria") @Size(min = 10, max = 64, message = "La contraseña debe tener entre 10 y 64 caracteres") String password,

            @Size(max = 2000, message = "Máximo 2000 caracteres") @Nullable String bio) {}

    /** Sustituye los datos editables; los del otro rol se ignoran. */
    record UpdateProfileRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 60, message = "Máximo 60 caracteres") String firstName,

            @NotBlank(message = "El apellido es obligatorio") @Size(max = 60, message = "Máximo 60 caracteres") String lastName,

            @Valid @Nullable ClientData client,

            @Size(max = 2000, message = "Máximo 2000 caracteres") @Nullable String bio) {}

    record ClientProfileResponse(
            @Nullable Sex sex,
            @Nullable LocalDate birthDate,
            @Nullable Integer age,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg) {}

    record NutritionistProfileResponse(@Nullable String bio) {}

    record MeResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            Role role,
            @Nullable ClientProfileResponse client,
            @Nullable NutritionistProfileResponse nutritionist) {}
}
