package com.healthybites.service.impl;

import com.healthybites.config.properties.StorageProperties;
import com.healthybites.service.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** Ficheros en disco, en {@code STORAGE_LOCATION}. Ninguna ruta puede salir de ese directorio. */
@Service
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final StorageProperties storageProperties;
    private Path rootLocation;

    @PostConstruct
    @Override
    public void init() {
        rootLocation = Paths.get(storageProperties.location().trim()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de almacenamiento", e);
        }
    }

    @Override
    public void store(InputStream content, String filename) {
        Path destination = resolve(filename)
                .orElseThrow(() -> new IllegalArgumentException("Nombre de fichero no válido"));
        try {
            Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el fichero", e);
        }
    }

    @Override
    public Optional<Resource> loadAsResource(String filename) {
        return resolve(filename)
                .filter(Files::isRegularFile)
                .filter(Files::isReadable)
                .map(FileSystemResource::new);
    }

    @Override
    public void delete(String filename) {
        resolve(filename).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo borrar el fichero", e);
            }
        });
    }

    /** Ruta dentro del directorio de almacenamiento; vacía si el nombre intenta salir de él. */
    private Optional<Path> resolve(String filename) {
        try {
            Path path = rootLocation.resolve(filename).normalize();
            return path.getParent() != null && path.getParent().equals(rootLocation) ? Optional.of(path) : Optional.empty();
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }
}
