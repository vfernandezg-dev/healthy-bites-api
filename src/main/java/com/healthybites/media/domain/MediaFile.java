package com.healthybites.media.domain;

import com.healthybites.media.MediaPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

/** Fichero subido. Inmutable: si cambia, se sube otro. */
@Entity
@Table(name = "media_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaFile {

    public enum Visibility {
        PUBLIC,
        PROTECTED
    }

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    /** Nombre en el almacenamiento ({@code <uuid>.<ext>}), generado por el servidor. */
    @Column(name = "storage_key", nullable = false, unique = true, length = 100)
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static MediaFile register(
            UUID ownerId,
            MediaPurpose purpose,
            Visibility visibility,
            String contentType,
            long sizeBytes,
            String sha256,
            String storageKey,
            Instant now) {
        MediaFile file = new MediaFile();
        file.ownerId = ownerId;
        file.purpose = purpose;
        file.visibility = visibility;
        file.contentType = contentType;
        file.sizeBytes = sizeBytes;
        file.sha256 = sha256;
        file.storageKey = storageKey;
        file.createdAt = now;
        return file;
    }

    public UUID requireId() {
        if (id == null) {
            throw new IllegalStateException("El fichero aún no tiene id");
        }
        return id;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    /** Imágenes y vídeos se muestran en la página; el resto (PDF) se descarga. */
    public boolean isInline() {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }
}
