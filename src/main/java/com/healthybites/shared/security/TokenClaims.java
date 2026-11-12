package com.healthybites.shared.security;

/** Claims propios del access token. {@code sub} es el id (UUID) del usuario. */
public final class TokenClaims {

    /** Roles del usuario, sin el prefijo {@code ROLE_} ({@code ["CLIENT"]}). */
    public static final String ROLES = "roles";

    private TokenClaims() {}
}
