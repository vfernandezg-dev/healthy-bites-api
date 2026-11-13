package com.healthybites.media.app;

import com.healthybites.media.MediaFiles;
import com.healthybites.media.MediaPurpose;
import com.healthybites.media.domain.FileType;
import com.healthybites.media.domain.MediaFile;
import com.healthybites.media.domain.MediaFileRepository;
import com.healthybites.shared.security.Role;
import com.healthybites.shared.web.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
 *   <li>Con un almacenamiento de objetos, el navegador sube el fichero directamente (URL prefirmada) y la API lo
 *       comprueba al confirmarlo: los ficheros grandes no pasan por la API, que en Vercel no admite peticiones de más
 *       de 4,5 MB.</li>
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
    private final UploadTickets tickets;
    private final Clock clock;

    public MediaService(
            MediaFileRepository files, FileStorage storage, MediaUrlSigner signer, UploadTickets tickets, Clock clock) {
        this.files = files;
        this.storage = storage;
        this.signer = signer;
        this.tickets = tickets;
        this.clock = clock;
    }

    /** Validez de la URL prefirmada del almacenamiento: los públicos se reutilizan más; los protegidos caducan antes. */
    static final Duration PUBLIC_DIRECT_READ = Duration.ofHours(1);

    static final Duration PROTECTED_DIRECT_READ = Duration.ofMinutes(5);

    /** Tiempo para subir el fichero con la URL prefirmada y confirmarlo. */
    static final Duration DIRECT_UPLOAD_WINDOW = Duration.ofMinutes(15);

    /**
     * Las subidas directas esperan con este prefijo hasta que se confirman; el almacenamiento borra las abandonadas
     * (regla de ciclo de vida del bucket).
     */
    public static final String PENDING_PREFIX = "pending-";

    /** URL para subir el fichero directamente al almacenamiento, el tipo que hay que enviar y el billete. */
    public record DirectUpload(URI uploadUrl, String contentType, String ticket, Instant expiresAt) {}

    /** Cómo se entrega un fichero autorizado. */
    public sealed interface Delivery {

        MediaFile file();

        /** El navegador lo lee directamente del almacenamiento (URL prefirmada que vale {@code validFor}). */
        record Redirect(MediaFile file, URI location, Duration validFor) implements Delivery {}

        /** La API sirve el contenido (almacenamiento en disco). */
        record Content(MediaFile file, Resource resource) implements Delivery {}
    }

    /**
     * Sin transacción propia: el fichero (hasta 100 MB) se escribe sin retener una conexión de la base de datos, y
     * solo el registro final abre una transacción corta ({@code save}). Si el registro falla, se borra el fichero.
     */
    public MediaFile upload(MultipartFile upload, MediaPurpose purpose, UUID ownerId, Role role) {
        authorize(purpose, role);
        if (upload.isEmpty()) {
            throw ApiException.badRequest("empty-file", "El fichero está vacío.");
        }
        Set<FileType> accepted = accepted(purpose);
        FileType type =
                FileType.detect(header(upload)).filter(accepted::contains).orElseThrow(() -> unsupported(accepted));
        checkSize(type, upload.getSize());

        String key = UUID.randomUUID() + "." + type.extension();
        String sha256;
        try (DigestInputStream content = new DigestInputStream(upload.getInputStream(), sha256Digest())) {
            storage.store(content, upload.getSize(), type.contentType(), key);
            sha256 = HexFormat.of().formatHex(content.getMessageDigest().digest());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el fichero subido", e);
        }
        return register(ownerId, purpose, type, upload.getSize(), sha256, key);
    }

    /**
     * Primer paso de una subida directa: con el tipo y el tamaño que declara el navegador (y las mismas reglas que una
     * subida a la API), una URL prefirmada para subir el fichero al almacenamiento y el billete para confirmarlo. Sin
     * almacenamiento de objetos responde 501 {@code direct-upload-unavailable}: el cliente sube a {@code POST /media}.
     */
    public DirectUpload startDirectUpload(
            MediaPurpose purpose, @Nullable String contentType, long size, UUID ownerId, Role role) {
        authorize(purpose, role);
        Set<FileType> accepted = accepted(purpose);
        FileType type = FileType.fromContentType(contentType)
                .filter(accepted::contains)
                .orElseThrow(() -> unsupported(accepted));
        if (size <= 0) {
            throw ApiException.badRequest("empty-file", "El fichero está vacío.");
        }
        checkSize(type, size);
        String key = PENDING_PREFIX + UUID.randomUUID() + "." + type.extension();
        URI url = storage.presignedPut(key, type.contentType(), size, DIRECT_UPLOAD_WINDOW)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_IMPLEMENTED,
                        "direct-upload-unavailable",
                        "Este servidor no admite subidas directas: sube el fichero a /media."));
        Instant expiresAt = clock.instant().plus(DIRECT_UPLOAD_WINDOW);
        String ticket = tickets.issue(new UploadTickets.Ticket(key, ownerId, purpose, type, size, expiresAt));
        return new DirectUpload(url, type.contentType(), ticket, expiresAt);
    }

    /**
     * Segundo paso: comprueba el fichero recibido como si se hubiera subido a la API (tipo por sus bytes, tamaño
     * exacto y SHA-256), lo pasa a su clave definitiva y lo registra. Si no es lo autorizado, se borra. El billete
     * solo sirve a quien lo pidió y una vez: después, el fichero ya no está en la clave pendiente. Sin transacción
     * propia, como {@link #upload}.
     */
    public MediaFile completeDirectUpload(@Nullable String ticketValue, UUID ownerId) {
        UploadTickets.Ticket ticket = tickets.verify(ticketValue)
                .filter(candidate -> candidate.ownerId().equals(ownerId))
                .orElseThrow(() -> ApiException.unprocessable(
                        "invalid-upload-ticket", "La subida no es válida o ha caducado. Vuelve a intentarlo."));
        Resource received = storage.load(ticket.storageKey())
                .orElseThrow(() -> ApiException.unprocessable(
                        "upload-not-received", "No ha llegado el fichero. Vuelve a subirlo."));
        Inspection inspection;
        try (InputStream content = received.getInputStream()) {
            inspection = inspect(content);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el fichero subido", e);
        }
        if (!inspection.type().map(ticket.type()::equals).orElse(false)) {
            storage.delete(ticket.storageKey());
            throw unsupported(accepted(ticket.purpose()));
        }
        if (inspection.size() != ticket.size()) {
            storage.delete(ticket.storageKey());
            throw ApiException.unprocessable(
                    "upload-size-mismatch", "El fichero recibido no coincide con el anunciado. Vuelve a subirlo.");
        }
        String key = UUID.randomUUID() + "." + ticket.type().extension();
        storage.move(ticket.storageKey(), key);
        return register(ownerId, ticket.purpose(), ticket.type(), ticket.size(), inspection.sha256(), key);
    }

    private static void authorize(MediaPurpose purpose, Role role) {
        if (purpose != MediaPurpose.POST_IMAGE && !STAFF.contains(role)) {
            throw ApiException.forbidden("media-purpose-forbidden", "No puedes subir ficheros de este tipo");
        }
    }

    private static Set<FileType> accepted(MediaPurpose purpose) {
        return purpose == MediaPurpose.CONTENT_FILE ? CONTENT : IMAGES;
    }

    private static ApiException unsupported(Set<FileType> accepted) {
        return new ApiException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "unsupported-file-type",
                accepted == IMAGES
                        ? "Solo se aceptan imágenes JPG, PNG, WebP o GIF."
                        : "Se aceptan imágenes JPG, PNG, WebP o GIF, PDF y vídeos MP4.");
    }

    private static void checkSize(FileType type, long size) {
        if (size > type.maxBytes()) {
            throw new ApiException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    "file-too-large",
                    "El fichero supera el máximo de " + type.maxMegabytes() + " MB para este tipo.");
        }
    }

    /** Registra el fichero ya guardado; si el registro falla, lo borra (sin registro no se serviría nunca). */
    private MediaFile register(
            UUID ownerId, MediaPurpose purpose, FileType type, long size, String sha256, String key) {
        MediaFile.Visibility visibility =
                purpose == MediaPurpose.CONTENT_FILE ? MediaFile.Visibility.PROTECTED : MediaFile.Visibility.PUBLIC;
        try {
            return files.save(MediaFile.register(
                    ownerId, purpose, visibility, type.contentType(), size, sha256, key, clock.instant()));
        } catch (RuntimeException e) {
            storage.delete(key);
            throw e;
        }
    }

    /** Tipo (por los primeros bytes), tamaño y SHA-256 de un contenido, leído una sola vez. */
    private record Inspection(Optional<FileType> type, long size, String sha256) {}

    private static Inspection inspect(InputStream content) throws IOException {
        DigestInputStream digesting = new DigestInputStream(content, sha256Digest());
        byte[] header = digesting.readNBytes(FileType.HEADER_BYTES);
        long size = header.length + digesting.transferTo(OutputStream.nullOutputStream());
        return new Inspection(
                FileType.detect(header),
                size,
                HexFormat.of().formatHex(digesting.getMessageDigest().digest()));
    }

    /**
     * El fichero, si está registrado y el acceso es válido (público, o protegido con una firma correcta y vigente):
     * como redirección a una URL prefirmada si el almacenamiento las ofrece, o con su contenido.
     */
    @Transactional(readOnly = true)
    public Optional<Delivery> open(UUID id, @Nullable Long expires, @Nullable String signature) {
        return files.findById(id)
                .filter(file -> file.isPublic() || signer.isValid(id, expires, signature))
                .flatMap(this::deliver);
    }

    private Optional<Delivery> deliver(MediaFile file) {
        Duration validFor = file.isPublic() ? PUBLIC_DIRECT_READ : PROTECTED_DIRECT_READ;
        FileStorage.DirectRead read = new FileStorage.DirectRead(
                file.getContentType(),
                MediaHeaders.disposition(file).toString(),
                MediaHeaders.cacheControl(file).getHeaderValue(),
                validFor);
        Optional<Delivery> redirect = storage.presignedGet(file.getStorageKey(), read)
                .map(location -> new Delivery.Redirect(file, location, validFor));
        return redirect.isPresent()
                ? redirect
                : storage.load(file.getStorageKey()).map(resource -> new Delivery.Content(file, resource));
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
