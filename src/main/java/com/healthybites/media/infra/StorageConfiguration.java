package com.healthybites.media.infra;

import com.healthybites.media.app.FileStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El almacenamiento de ficheros según {@code healthybites.storage.type}. */
@Configuration(proxyBeanMethods = false)
class StorageConfiguration {

    @Bean
    FileStorage fileStorage(StorageProperties properties) {
        return switch (properties.type()) {
            case LOCAL -> new LocalFileStorage(properties.location());
            case S3 -> S3FileStorage.create(properties.s3());
        };
    }
}
