package com.healthybites.media;

import java.time.Duration;
import java.util.UUID;

/** Ficheros para otros módulos: validar los que se referencian y construir sus URLs. */
public interface MediaFiles {

    /**
     * Comprueba que el fichero existe, lo subió {@code ownerId} y tiene ese propósito (por ejemplo, que la portada de
     * un contenido es una portada subida por su autor).
     *
     * @throws com.healthybites.shared.web.ApiException 422 {@code invalid-media} si no se cumple
     */
    void requireOwned(UUID mediaId, UUID ownerId, MediaPurpose purpose);

    /** Ruta de un fichero público, relativa a la API ({@code /media/{id}}). */
    String publicPath(UUID mediaId);

    /** Ruta firmada de un fichero protegido, válida durante {@code ttl}. Solo tras comprobar el acceso. */
    String signedPath(UUID mediaId, Duration ttl);
}
