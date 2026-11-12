package com.healthybites.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Fichero subido. {@code GET /media/{filename}} solo sirve ficheros registrados aquí, con el tipo que se detectó al
 * subirlos (por sus bytes, no por la extensión ni por lo que declaró el navegador).
 */
@Entity
@Table(name = "media_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre en disco, generado por el servidor ({@code <uuid>.<ext>}). */
    @Column(nullable = false, unique = true, length = 64)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static MediaFile register(String filename, String contentType, long sizeBytes, String sha256,
                                     Integer uploadedBy, Instant now) {
        MediaFile file = new MediaFile();
        file.filename = filename;
        file.contentType = contentType;
        file.sizeBytes = sizeBytes;
        file.sha256 = sha256;
        file.uploadedBy = uploadedBy;
        file.createdAt = now;
        return file;
    }
}
