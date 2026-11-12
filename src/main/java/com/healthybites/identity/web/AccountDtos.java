package com.healthybites.identity.web;

import com.healthybites.shared.validation.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpos de la cuenta del propio usuario ({@code /me/password}). */
final class AccountDtos {

    private AccountDtos() {}

    record ChangePasswordRequest(
            @NotBlank(message = "La contraseña actual es obligatoria") @Size(max = 128) String currentPassword,

            @NotBlank(message = "La nueva contraseña es obligatoria") @Password
            String newPassword) {}
}
