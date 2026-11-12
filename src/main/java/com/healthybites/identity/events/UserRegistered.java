package com.healthybites.identity.events;

import com.healthybites.identity.Role;
import java.util.UUID;

/** Se ha creado una cuenta (registro de un cliente o invitación aceptada por un nutricionista). */
public record UserRegistered(UUID userId, Role role) {}
