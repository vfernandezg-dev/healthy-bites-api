package com.healthybites.identity.app;

import java.time.Duration;

/**
 * Se ha emitido (o reenviado) una invitación. Evento interno que solo vive en memoria: el enlace lleva el token en
 * claro, así que nunca se persiste.
 */
public record InvitationIssued(String email, String firstName, String link, Duration validFor) {

    @Override
    public String toString() {
        return "InvitationIssued[email=" + email + ", validFor=" + validFor + "]";
    }
}
