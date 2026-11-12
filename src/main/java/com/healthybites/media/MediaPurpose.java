package com.healthybites.media;

/** Para qué se sube un fichero: decide quién puede subirlo, qué tipos se aceptan y si es público. */
public enum MediaPurpose {
    /** Imagen de una publicación de la comunidad (cualquier usuario). Pública. */
    POST_IMAGE,
    /** Portada de un contenido (nutricionistas). Pública: se ve en el catálogo aunque el contenido sea de pago. */
    CONTENT_COVER,
    /** Fichero de un contenido: vídeo, PDF o imagen (nutricionistas). Protegido: solo con URL firmada. */
    CONTENT_FILE
}
