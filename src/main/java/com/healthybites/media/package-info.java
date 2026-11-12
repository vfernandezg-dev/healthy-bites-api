/**
 * Ficheros subidos: imágenes de publicaciones, portadas y ficheros de contenido. El tipo se detecta por los bytes y
 * el nombre en disco lo genera el servidor.
 *
 * <p>Los ficheros PUBLIC se sirven por id ({@code GET /media/{id}}); los PROTECTED solo con una URL firmada (HMAC) y
 * de vida corta que genera el módulo dueño del recurso tras comprobar el acceso: un {@code <img>} o un
 * {@code <video>} no pueden enviar el access token, que vive en memoria.
 *
 * <p>API para otros módulos: {@link com.healthybites.media.MediaFiles}.
 */
@ApplicationModule(displayName = "Media")
@NullMarked
package com.healthybites.media;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
