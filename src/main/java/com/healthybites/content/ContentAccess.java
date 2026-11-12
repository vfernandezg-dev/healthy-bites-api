package com.healthybites.content;

import java.util.Optional;
import java.util.UUID;

/** Para rewards: comprobar contenidos de un nutricionista y desbloquearlos como premio. */
public interface ContentAccess {

    /** ¿El contenido existe, está publicado y es de ese nutricionista? */
    boolean isPublishedBy(UUID contentItemId, UUID authorId);

    /** Desbloquea el contenido para el usuario. No falla si ya lo tenía. */
    void grant(UUID contentItemId, UUID userId);

    Optional<String> titleOf(UUID contentItemId);
}
