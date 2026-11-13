package com.healthybites.media.app;

import com.healthybites.media.MediaPurpose;
import com.healthybites.media.domain.FileType;
import com.healthybites.shared.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Billetes de las subidas directas al almacenamiento: dicen quién sube, para qué, de qué tipo y tamaño, con qué clave
 * y hasta cuándo, firmados con HMAC. Así, al confirmar la subida, la API sabe qué esperar sin guardar nada antes.
 *
 * <p>La clave se deriva de {@code JWT_SECRET} con una etiqueta propia (separación de dominios), como en
 * {@link MediaUrlSigner}: un billete nunca vale como firma de una URL ni de un JWT.
 */
@Component
public class UploadTickets {

    private static final String ALGORITHM = "HmacSHA256";
    private static final byte[] CONTEXT = "healthybites/media-upload/v1".getBytes(StandardCharsets.UTF_8);
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final SecretKeySpec key;
    private final Clock clock;

    public UploadTickets(JwtProperties jwt, Clock clock) {
        this.key = new SecretKeySpec(
                hmac(new SecretKeySpec(Objects.requireNonNull(jwt.decodedSecret()), ALGORITHM), CONTEXT), ALGORITHM);
        this.clock = clock;
    }

    /** Lo que se autorizó subir. */
    public record Ticket(
            String storageKey, UUID ownerId, MediaPurpose purpose, FileType type, long size, Instant expiresAt) {}

    public String issue(Ticket ticket) {
        String payload = String.join(
                "|",
                ticket.storageKey(),
                ticket.ownerId().toString(),
                ticket.purpose().name(),
                ticket.type().name(),
                Long.toString(ticket.size()),
                Long.toString(ticket.expiresAt().getEpochSecond()));
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        return ENCODER.encodeToString(bytes) + "." + ENCODER.encodeToString(hmac(key, bytes));
    }

    /** El billete, si la firma es correcta y no ha caducado; vacío si no. */
    public Optional<Ticket> verify(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }
        int dot = value.indexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        try {
            byte[] payload = DECODER.decode(value.substring(0, dot));
            byte[] signature = DECODER.decode(value.substring(dot + 1));
            if (!MessageDigest.isEqual(hmac(key, payload), signature)) {
                return Optional.empty();
            }
            String[] fields = new String(payload, StandardCharsets.UTF_8).split("\\|", -1);
            if (fields.length != 6) {
                return Optional.empty();
            }
            Ticket ticket = new Ticket(
                    fields[0],
                    UUID.fromString(fields[1]),
                    MediaPurpose.valueOf(fields[2]),
                    FileType.valueOf(fields[3]),
                    Long.parseLong(fields[4]),
                    Instant.ofEpochSecond(Long.parseLong(fields[5])));
            return ticket.expiresAt().isAfter(clock.instant()) ? Optional.of(ticket) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static byte[] hmac(SecretKeySpec key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC no disponible", e);
        }
    }
}
