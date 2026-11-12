package com.healthybites.identity.events;

import java.util.UUID;

/** Un ADMIN ha desactivado una cuenta: no puede iniciar sesión y sus sesiones abiertas están revocadas. */
public record UserDeactivated(UUID userId) {}
