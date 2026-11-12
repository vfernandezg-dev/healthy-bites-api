package com.healthybites.shared.web;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Cabecera {@code Location} de un recurso recién creado (201): URL absoluta que incluye el context path
 * ({@code /api/v1}), sea cual sea el host por el que llegó la petición.
 */
public final class Locations {

    private Locations() {}

    /** @param path ruta dentro de la API con sus variables, por ejemplo {@code /groups/{id}} */
    public static URI of(String path, Object... variables) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(path)
                .buildAndExpand(variables)
                .toUri();
    }
}
