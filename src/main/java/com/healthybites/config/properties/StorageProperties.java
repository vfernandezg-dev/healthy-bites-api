package com.healthybites.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Almacenamiento de los ficheros subidos ({@code STORAGE_LOCATION}). Nunca debe apuntar a {@code mediafiles/}.
 *
 * @param location carpeta donde se guardan los ficheros; relativa al directorio de trabajo o absoluta
 */
@Validated
@ConfigurationProperties("healthybites.storage")
public record StorageProperties(@NotBlank String location) {
}
