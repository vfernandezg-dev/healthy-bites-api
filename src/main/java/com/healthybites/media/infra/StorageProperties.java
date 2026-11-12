package com.healthybites.media.infra;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Dónde se guardan los ficheros subidos ({@code healthybites.storage.*}). */
@ConfigurationProperties("healthybites.storage")
public record StorageProperties(
        @DefaultValue("local") Type type,
        @DefaultValue("uploads") String location,
        @DefaultValue S3 s3) {

    public enum Type {
        /** Una carpeta del servidor: solo con una instancia y disco persistente. */
        LOCAL,
        /** Almacenamiento de objetos S3-compatible, compartido por todas las instancias. */
        S3
    }

    public record S3(
            @Nullable String endpoint,
            @Nullable String bucket,
            @Nullable String accessKey,
            @Nullable String secretKey,
            @DefaultValue("auto") String region,
            @DefaultValue("true") boolean pathStyle) {}
}
