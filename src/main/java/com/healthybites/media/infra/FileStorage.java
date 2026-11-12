package com.healthybites.media.infra;

import java.io.InputStream;
import java.util.Optional;
import org.springframework.core.io.Resource;

/**
 * Almacenamiento de los ficheros. Hoy en disco ({@link LocalFileStorage}); un almacenamiento de objetos (S3, R2)
 * solo necesita otra implementación.
 */
public interface FileStorage {

    /** Guarda el contenido con esa clave (generada por el servidor). */
    void store(InputStream content, String key);

    /** El contenido, si existe y se puede leer. */
    Optional<Resource> load(String key);

    void delete(String key);
}
