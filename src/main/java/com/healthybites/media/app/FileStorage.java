package com.healthybites.media.app;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import org.springframework.core.io.Resource;

/**
 * Almacenamiento de los ficheros (puerto de salida): en disco ({@code infra.LocalFileStorage}) o en uno de objetos
 * S3-compatible ({@code infra.S3FileStorage}), según {@code healthybites.storage.type}.
 */
public interface FileStorage {

    /** Guarda el contenido con esa clave (generada por el servidor). */
    void store(InputStream content, long size, String contentType, String key);

    /** El contenido, si existe y se puede leer. */
    Optional<Resource> load(String key);

    void delete(String key);

    /**
     * URL temporal para que el navegador lea el fichero directamente del almacenamiento, con las cabeceras indicadas;
     * vacío si el almacenamiento no las ofrece (en disco, la API sirve el contenido).
     */
    default Optional<URI> presignedGet(String key, DirectRead read) {
        return Optional.empty();
    }

    /** Cómo debe responder el almacenamiento a la URL prefirmada, y durante cuánto tiempo vale. */
    record DirectRead(String contentType, String contentDisposition, String cacheControl, Duration validFor) {}

    /**
     * URL temporal para que el navegador suba el contenido directamente al almacenamiento (un PUT con exactamente ese
     * tipo y ese tamaño), sin pasar por la API; vacío si el almacenamiento no las ofrece (en disco, se sube a la API).
     */
    default Optional<URI> presignedPut(String key, String contentType, long size, Duration validFor) {
        return Optional.empty();
    }

    /** Cambia la clave de un fichero guardado (de la subida pendiente a la definitiva). */
    void move(String from, String to);
}
