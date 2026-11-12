package com.healthybites.service.media;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * Tipos de fichero que se aceptan, reconocidos por sus primeros bytes ("magic numbers"). La extensión y el
 * {@code Content-Type} que envía el navegador no cuentan: los controla quien sube el fichero. SVG y HTML quedan
 * fuera a propósito, porque pueden llevar scripts.
 */
public enum FileType {

    JPEG("image/jpeg", "jpg", 5),
    PNG("image/png", "png", 5),
    GIF("image/gif", "gif", 5),
    WEBP("image/webp", "webp", 5),
    PDF("application/pdf", "pdf", 20),
    MP4("video/mp4", "mp4", 100);

    /** Bytes que hacen falta para reconocer cualquiera de los tipos. */
    public static final int HEADER_BYTES = 16;

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final Set<String> MP4_BRANDS =
            Set.of("isom", "iso2", "iso4", "iso5", "iso6", "mp41", "mp42", "avc1", "M4V ", "dash", "MSNV");

    private final String contentType;
    private final String extension;
    private final long maxBytes;

    FileType(String contentType, String extension, int maxMegabytes) {
        this.contentType = contentType;
        this.extension = extension;
        this.maxBytes = maxMegabytes * 1024L * 1024L;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    public long maxBytes() {
        return maxBytes;
    }

    public long maxMegabytes() {
        return maxBytes / (1024L * 1024L);
    }

    public boolean isImage() {
        return contentType.startsWith("image/");
    }

    public static Optional<FileType> detect(byte[] header) {
        if (startsWith(header, 0, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return Optional.of(JPEG);
        }
        if (startsWith(header, 0, PNG_SIGNATURE)) {
            return Optional.of(PNG);
        }
        if (startsWith(header, 0, ascii("GIF87a")) || startsWith(header, 0, ascii("GIF89a"))) {
            return Optional.of(GIF);
        }
        if (startsWith(header, 0, ascii("RIFF")) && startsWith(header, 8, ascii("WEBP"))) {
            return Optional.of(WEBP);
        }
        if (startsWith(header, 0, ascii("%PDF-"))) {
            return Optional.of(PDF);
        }
        if (startsWith(header, 4, ascii("ftyp")) && header.length >= 12
                && MP4_BRANDS.contains(new String(header, 8, 4, StandardCharsets.US_ASCII))) {
            return Optional.of(MP4);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] data, int offset, byte[] prefix) {
        if (data.length < offset + prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[offset + i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }
}
