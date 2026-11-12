package com.healthybites.community;

import java.util.Optional;
import java.util.UUID;

/** Datos de un grupo para otros módulos (por ejemplo, el aviso de una solicitud para entrar). */
public interface GroupDirectory {

    Optional<String> nameOf(UUID groupId);
}
