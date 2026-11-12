package com.healthybites.identity;

import java.util.UUID;

/** Vista de solo lectura de una cuenta para otros módulos. Nunca incluye el hash de la contraseña. */
public record UserAccount(UUID id, String email, String firstName, String lastName, Role role, boolean active) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
