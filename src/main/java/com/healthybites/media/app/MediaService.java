package com.healthybites.media.app;

import com.healthybites.identity.Role;
import com.healthybites.media.MediaFiles;
import com.healthybites.media.MediaPurpose;
import com.healthybites.media.domain.FileType;
import com.healthybites.media.domain.MediaFile;
import com.healthybites.media.domain.MediaFileRepository;
import com.healthybites.media.infra.FileStorage;
import com.healthybites.shared.web.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Subida y lectura de ficheros.
 *
 * <ul>
 *   <li>Cada propósito define quién sube, qué tipos se aceptan (por sus bytes) y si el fichero es público.</li>
 *   <li>El nombre en disco lo genera el servidor, con la extensión del tipo real.</li>
 *   <li>Los protegidos solo se leen con una URL firmada y sin caducar.</li>
 * </ul>
 */
@Service
public class MediaService implements MediaFiles {

    private static final Set<FileType> IMAGES = EnumSet.of(FileType.JPEG, FileType.PNG, FileType.GIF, FileType.WEBP);
    private static final Set<FileType> CONTENT = EnumSet.allOf(FileType.class);
    private static final Set<Role> STAFF = EnumSet.of(Role.NUTRITIONIST, Role.ADMIN);

    private final MediaFileRepository files;
    private final FileStorage storage;
    private final MediaUrlSigner signer;
    private final Clock clock;

    public MediaService(MediaFileRepository files, FileStorage storage, MediaUrlSigner signer, Clock clock) {
        this.files = files;
        this.storage = storage;
        this.signer = signer;
        this.clock = clock;
    }

    /** Un fichero registrado y su contenido. */
    public record StoredMedia(MediaFile file, Resource resource) {}

    @Transactional
    public MediaFile upload(MultipartFile upload, MediaPurpose purpose, UUID ownerId, Role role) {
        if (purpose != MediaPurpose.POST_IMAGE && !STAFF.contains(role)) {
            throw ApiException.forbidden("media-purpose-forbidden", "No puedes subir ficheros de este tipo");
        }
        if (upload.isEmpty()) {
            throw ApiException.badRequest("empty-file", "El fichero está vacío.");
        }
        Set<FileType> accepted = purpose == MediaPurpose.CONTENT_FILE ? CONTENT : IMAGES;
        FileType type = FileType.detect(header(upload))
                .filter(accepted::contains)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "unsupported-file-type",
                        accepted == IMAGES
                                ? "Solo se aceptan imágenes JPG, PNG, WebP o GIF."
                                : "Se aceptan imágenes JPG, PNG, WebP o GIF, PDF y vídeos MP4."));
        if (upload.getSize() > type.maxBytes()) {
            throw new ApiException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    "file-too-large",
                    "El fichero supera el máximo de " + type.maxMegabytes() + " MB para este tipo.");
        }

        String key = UUID.randomUUID() + "." + type.extension();
        String sha256;
        try (DigestInputStream content = new DigestInputStream(upload.getInputStream(), sha256Digest())) {
            storage.store(content, key);
            sha256 = HexFormat.of().formatHex(content.getMessageDigest().digest());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el fichero subido", e);
        }
        MediaFile.Visibility visibility =
                purpose == MediaPurpose.CONTENT_FILE ? MediaFile.Visibility.PROTECTED : MediaFile.Visibility.PUBLIC;
        try {
            return files.save(MediaFile.register(
                    ownerId, purpose, visibility, type.contentType(), upload.getSize(), sha256, key, clock.instant()));
        } catch (RuntimeException e) {
            storage.delete(key); // sin registro, el fichero no se serviría nunca
            throw e;
        }
    }

    /**
     * El fichero, si está registrado y el acceso es válido: público, o protegido con una firma correcta y vigente.
     */
    @Transactional(readOnly = true)
    public Optional<StoredMedia> open(UUID id, @Nullable Long expires, @Nullable String signature) {
        return files.findById(id)
                .filter(file -> file.isPublic() || signer.isValid(id, expires, signature))
                .flatMap(file -> storage.load(file.getStorageKey()).map(resource -> new StoredMedia(file, resource)));
    }

    @Override
    @Transactional(readOnly = true)
    public void requireOwned(UUID mediaId, UUID ownerId, MediaPurpose purpose) {
        boolean valid = files.findById(mediaId)
                .filter(file -> file.getOwnerId().equals(ownerId) && file.getPurpose() == purpose)
                .isPresent();
        if (!valid) {
            throw ApiException.unprocessable("invalid-media", "El fichero no existe o no corresponde a este campo");
        }
    }

    @Override
    public String publicPath(UUID mediaId) {
        return "/media/" + mediaId;
    }

    @Override
    public String signedPath(UUID mediaId, Duration ttl) {
        return signer.sign(mediaId, ttl);
    }

    private static byte[] header(MultipartFile upload) {
        try (InputStream in = upload.getInputStream()) {
            return in.readNBytes(FileType.HEADER_BYTES);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el fichero subido", e);
        }
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
