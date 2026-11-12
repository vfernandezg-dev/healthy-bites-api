package com.healthybites.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Fichero subido que no se acepta: tipo no permitido (415), demasiado grande (413) o vacío (400). */
@Getter
public class FileRejectedException extends RuntimeException {

    private final HttpStatus status;

    public FileRejectedException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
