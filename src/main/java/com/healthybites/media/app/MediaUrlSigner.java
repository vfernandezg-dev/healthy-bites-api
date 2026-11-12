package com.healthybites.media.app;

import com.healthybites.shared.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * URLs firmadas de los ficheros protegidos: {@code /media/{id}?expires=<epoch>&signature=<hmac>}. La clave se deriva
 * de {@code JWT_SECRET} con una etiqueta propia (separación de dominios), así que no hace falta otro secreto y una
 * firma de URL nunca vale como firma de un JWT ni al revés.
 */
@Component
public class MediaUrlSigner {

    private static final String ALGORITHM = "HmacSHA256";
    private static final byte[] CONTEXT = "healthybites/media-url/v1".getBytes(StandardCharsets.UTF_8);

    private final SecretKeySpec key;
    private final Clock clock;

    public MediaUrlSigner(JwtProperties jwt, Clock clock) {
        this.key = new SecretKeySpec(
                hmac(new SecretKeySpec(Objects.requireNonNull(jwt.decodedSecret()), ALGORITHM), CONTEXT), ALGORITHM);
        this.clock = clock;
    }

    public String sign(UUID mediaId, Duration ttl) {
        long expires = clock.instant().plus(ttl).getEpochSecond();
        return "/media/" + mediaId + "?expires=" + expires + "&signature=" + signature(mediaId, expires);
    }

    /** Firma correcta y sin caducar. */
    public boolean isValid(UUID mediaId, @Nullable Long expires, @Nullable String signature) {
        if (expires == null || signature == null) {
            return false;
        }
        if (Instant.ofEpochSecond(expires).isBefore(clock.instant())) {
            return false;
        }
        byte[] expected = signature(mediaId, expires).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, signature.getBytes(StandardCharsets.US_ASCII));
    }

    private String signature(UUID mediaId, long expires) {
        byte[] mac = hmac(key, (mediaId + ":" + expires).getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac);
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
