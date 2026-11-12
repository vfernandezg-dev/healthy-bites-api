package com.healthybites.identity;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Una persona tal como se muestra en los listados (autor de un contenido o un plan, miembro, autor de un post). */
public record PersonResponse(UUID id, String name) {

    public static @Nullable PersonResponse of(@Nullable UserAccount account) {
        return account == null ? null : new PersonResponse(account.id(), account.fullName());
    }
}
