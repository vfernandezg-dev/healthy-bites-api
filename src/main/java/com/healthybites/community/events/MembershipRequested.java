package com.healthybites.community.events;

import java.util.UUID;

/** Alguien pidió entrar en un grupo privado: el creador tiene que aprobarlo. */
public record MembershipRequested(UUID groupId, UUID requesterId, UUID ownerId) {}
