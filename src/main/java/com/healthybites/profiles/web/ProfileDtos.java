package com.healthybites.profiles.web;

import com.healthybites.profiles.Sex;
import com.healthybites.profiles.app.ProfileService;
import com.healthybites.profiles.domain.ClientProfile;
import com.healthybites.profiles.domain.NutritionistProfile;
import com.healthybites.shared.security.Role;
import com.healthybites.shared.validation.EmailAddress;
import com.healthybites.shared.validation.Password;
import com.healthybites.shared.validation.PersonName;
import com.healthybites.shared.validation.WeightKg;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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

            @WeightKg @Nullable BigDecimal weightKg) {

        ProfileService.ClientData toData() {
            return new ProfileService.ClientData(sex, birthDate, heightCm, weightKg);
        }

        /** Sin datos físicos en la petición: todos vacíos. */
        static ProfileService.ClientData toDataOrEmpty(@Nullable ClientData data) {
            return data == null ? new ProfileService.ClientData(null, null, null, null) : data.toData();
        }
    }

    record RegisterRequest(
            @EmailAddress String email,

            @NotBlank(message = "La contraseña es obligatoria") @Password
            String password,

            @NotBlank(message = "El nombre es obligatorio") @PersonName
            String firstName,

            @NotBlank(message = "El apellido es obligatorio") @PersonName
            String lastName,

            @Valid @Nullable ClientData profile) {}

    record InvitationPreviewRequest(
            @NotBlank(message = "La invitación no es válida") @Size(max = 128) String token) {}

    record InvitationPreviewResponse(String email, String firstName, String lastName) {}

    record AcceptInvitationRequest(
            @NotBlank(message = "La invitación no es válida") @Size(max = 128) String token,

            @NotBlank(message = "La contraseña es obligatoria") @Password
            String password,

            @Size(max = 2000, message = "Máximo 2000 caracteres") @Nullable String bio) {}

    /** Sustituye los datos editables; los del otro rol se ignoran. */
    record UpdateProfileRequest(
            @NotBlank(message = "El nombre es obligatorio") @PersonName
            String firstName,

            @NotBlank(message = "El apellido es obligatorio") @PersonName
            String lastName,

            @Valid @Nullable ClientData client,

            @Size(max = 2000, message = "Máximo 2000 caracteres") @Nullable String bio) {}

    record ClientProfileResponse(
            @Nullable Sex sex,
            @Nullable LocalDate birthDate,
            @Nullable Integer age,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg) {

        /** La edad se calcula con la fecha de hoy (del reloj de la aplicación), no se guarda. */
        static @Nullable ClientProfileResponse of(@Nullable ClientProfile profile, LocalDate today) {
            return profile == null
                    ? null
                    : new ClientProfileResponse(
                            profile.getSex(),
                            profile.getBirthDate(),
                            profile.ageOn(today),
                            profile.getHeightCm(),
                            profile.getWeightKg());
        }
    }

    record NutritionistProfileResponse(@Nullable String bio) {

        static @Nullable NutritionistProfileResponse of(@Nullable NutritionistProfile profile) {
            return profile == null ? null : new NutritionistProfileResponse(profile.getBio());
        }
    }

    record MeResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            Role role,
            @Nullable ClientProfileResponse client,
            @Nullable NutritionistProfileResponse nutritionist) {

        static MeResponse of(ProfileService.Me me) {
            return new MeResponse(
                    me.account().id(),
                    me.account().email(),
                    me.account().firstName(),
                    me.account().lastName(),
                    me.account().role(),
                    ClientProfileResponse.of(me.client(), me.today()),
                    NutritionistProfileResponse.of(me.nutritionist()));
        }
    }
}
