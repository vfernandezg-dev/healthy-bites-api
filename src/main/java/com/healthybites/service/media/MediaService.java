package com.healthybites.service.media;

import com.healthybites.exception.FileRejectedException;
import com.healthybites.model.entity.MediaFile;
import com.healthybites.repository.MediaFileRepository;
import com.healthybites.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Subida y lectura de ficheros.
 *
 * <ul>
 *   <li>Solo se aceptan los tipos de {@link FileType}, reconocidos por sus bytes. Los clientes solo suben imágenes;
 *       nutricionistas y ADMIN, también PDF y vídeo.</li>
 *   <li>El nombre en disco lo genera el servidor, con la extensión del tipo detectado.</li>
 *   <li>Solo se sirve lo que está registrado en {@code media_files}, con el tipo guardado al subirlo.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MediaService {

    private static final Set<FileType> CLIENT_TYPES = EnumSet.of(FileType.JPEG, FileType.PNG, FileType.GIF, FileType.WEBP);
    private static final Pattern STORED_NAME =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[a-z0-9]{2,4}");

    private final StorageService storage;
    private final MediaFileRepository mediaFiles;
    private final Clock clock;

    /** Un fichero registrado y su contenido. */
    public record StoredMedia(MediaFile file, Resource resource) {
    }

    public MediaFile upload(MultipartFile upload, Integer uploadedBy, boolean staff) {
        if (upload.isEmpty()) {
            throw new FileRejectedException(HttpStatus.BAD_REQUEST, "El fichero está vacío.");
        }
        FileType type = FileType.detect(header(upload))
                .filter(detected -> staff || CLIENT_TYPES.contains(detected))
                .orElseThrow(() -> new FileRejectedException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, staff
                        ? "Tipo de fichero no permitido. Se aceptan imágenes JPG, PNG, WebP o GIF, PDF y vídeos MP4."
                        : "Tipo de fichero no permitido. Se aceptan imágenes JPG, PNG, WebP o GIF."));
        if (upload.getSize() > type.maxBytes()) {
            throw new FileRejectedException(HttpStatus.CONTENT_TOO_LARGE,
                    "El fichero supera el máximo de " + type.maxMegabytes() + " MB para este tipo.");
        }

        String filename = UUID.randomUUID() + "." + type.extension();
        String sha256;
        try (DigestInputStream content = new DigestInputStream(upload.getInputStream(), sha256())) {
            storage.store(content, filename);
            sha256 = HexFormat.of().formatHex(content.getMessageDigest().digest());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el fichero subido", e);
        }

        try {
            return mediaFiles.save(MediaFile.register(filename, type.contentType(), upload.getSize(), sha256,
                    uploadedBy, clock.instant()));
        } catch (RuntimeException e) {
            storage.delete(filename); // sin registro, el fichero no se serviría nunca: fuera del disco también
            throw e;
        }
    }

    /** El fichero si está registrado y sigue en disco. Cualquier otro nombre (rutas, ficheros ajenos) no existe. */
    public Optional<StoredMedia> open(String filename) {
        if (filename == null || !STORED_NAME.matcher(filename).matches()) {
            return Optional.empty();
        }
        return mediaFiles.findByFilename(filename)
                .flatMap(file -> storage.loadAsResource(filename).map(resource -> new StoredMedia(file, resource)));
    }

    /** Si el tipo guardado se muestra en la página o se descarga. */
    public static boolean isInline(MediaFile file) {
        return file.getContentType().startsWith("image/") || file.getContentType().startsWith("video/");
    }

    private static byte[] header(MultipartFile upload) {
        try (InputStream in = upload.getInputStream()) {
            return in.readNBytes(FileType.HEADER_BYTES);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el fichero subido", e);
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
