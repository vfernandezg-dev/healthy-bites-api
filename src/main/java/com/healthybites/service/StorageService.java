package com.healthybites.service;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Optional;

/** Almacenamiento de ficheros subidos. Los nombres los genera siempre el servidor. */
public interface StorageService {

    void init();

    /** Guarda el contenido con ese nombre, dentro del directorio de almacenamiento. */
    void store(InputStream content, String filename);

    /** El fichero, si existe y se puede leer. */
    Optional<Resource> loadAsResource(String filename);

    void delete(String filename);
}
