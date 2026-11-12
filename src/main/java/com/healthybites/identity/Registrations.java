package com.healthybites.identity;

/**
 * Altas de cuentas, para el módulo profiles (que crea el perfil de cada rol en la misma transacción). Los clientes
 * se registran solos; los nutricionistas, solo aceptando una invitación de un ADMIN.
 */
public interface Registrations {

    /**
     * @throws com.healthybites.shared.web.ApiException 409 {@code email-taken} si el correo ya tiene cuenta
     */
    UserAccount registerClient(String email, String rawPassword, String firstName, String lastName);

    /**
     * Datos de la invitación para mostrarlos antes de aceptar.
     *
     * @throws com.healthybites.shared.web.ApiException 400 {@code invalid-invitation} si no existe, caducó, se revocó
     *     o ya se usó
     */
    InvitationPreview previewInvitation(String token);

    /** Crea la cuenta de nutricionista y consume la invitación. Mismos errores que {@link #previewInvitation}. */
    UserAccount acceptInvitation(String token, String rawPassword);

    record InvitationPreview(String email, String firstName, String lastName) {}
}
