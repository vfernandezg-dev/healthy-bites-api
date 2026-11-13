package com.healthybites.media.infra;

import com.healthybites.media.app.FileStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/** Ficheros en disco, en {@code STORAGE_LOCATION}. Ninguna clave puede salir de ese directorio. */
class LocalFileStorage implements FileStorage {

    private final Path root;

    LocalFileStorage(String location) {
        this.root = Paths.get(location.trim()).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de almacenamiento", e);
        }
    }

    @Override
    public void store(InputStream content, long size, String contentType, String key) {
        Path destination = resolve(key).orElseThrow(() -> new IllegalArgumentException("Clave de fichero no válida"));
        try {
            Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el fichero", e);
        }
    }

    @Override
    public Optional<Resource> load(String key) {
        return resolve(key)
                .filter(Files::isRegularFile)
                .filter(Files::isReadable)
                .map(FileSystemResource::new);
    }

    @Override
    public void delete(String key) {
        resolve(key).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo borrar el fichero", e);
            }
        });
    }

    @Override
    public void move(String from, String to) {
        Path source = resolve(from).orElseThrow(() -> new IllegalArgumentException("Clave de fichero no válida"));
        Path target = resolve(to).orElseThrow(() -> new IllegalArgumentException("Clave de fichero no válida"));
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo mover el fichero", e);
        }
    }

    /** Ruta dentro del directorio de almacenamiento; vacía si la clave intenta salir de él. */
    private Optional<Path> resolve(String key) {
        try {
            Path path = root.resolve(key).normalize();
            return root.equals(path.getParent()) ? Optional.of(path) : Optional.empty();
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }
}
