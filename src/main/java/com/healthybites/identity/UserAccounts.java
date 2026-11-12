package com.healthybites.identity;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Consulta de cuentas para otros módulos (por ejemplo, el nombre del autor de un plan). */
public interface UserAccounts {

    Optional<UserAccount> findById(UUID id);

    /** Varias cuentas de una vez (evita N consultas al pintar listados). Los ids que no existen no aparecen. */
    Map<UUID, UserAccount> findAllById(Collection<UUID> ids);

    /**
     * De entre {@code ids}, las cuentas cuyo nombre, apellidos o correo contienen el texto, sin distinguir mayúsculas
     * ni tildes.
     */
    Set<UUID> matching(Collection<UUID> ids, String text);

    /** Cambia el nombre que se muestra (lo edita el usuario desde su perfil). */
    UserAccount rename(UUID id, String firstName, String lastName);
}
