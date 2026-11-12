package com.healthybites.media.app;

import com.healthybites.media.domain.MediaFile;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;

/**
 * Cabeceras con las que se entrega un fichero, las sirva la API (disco) o el almacenamiento con una URL prefirmada.
 */
public final class MediaHeaders {

    private MediaHeaders() {}

    /** Imágenes y vídeos se muestran; el resto se descarga con el nombre del servidor. */
    public static ContentDisposition disposition(MediaFile file) {
        return file.isInline()
                ? ContentDisposition.inline().build()
                : ContentDisposition.attachment().filename(file.getStorageKey()).build();
    }

    /** El contenido de un id no cambia nunca: los públicos se cachean un año; los protegidos, poco y en privado. */
    public static CacheControl cacheControl(MediaFile file) {
        return file.isPublic()
                ? CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable()
                : CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate();
    }
}
