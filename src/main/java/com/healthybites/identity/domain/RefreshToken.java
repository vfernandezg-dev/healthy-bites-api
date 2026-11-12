package com.healthybites.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

/**
 * Refresh token de una sesión. Solo se guarda el hash SHA-256 del valor: quien lea la base de datos no puede usar
 * los tokens. Cada rotación crea un token nuevo en la misma familia (la sesión) y revoca el anterior.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "family_expires_at", nullable = false)
    private Instant familyExpiresAt;

    @Column(name = "revoked_at")
    private @Nullable Instant revokedAt;

    @Column(name = "replaced_by_id")
    private @Nullable UUID replacedById;

    public static RefreshToken issue(
            User user, UUID familyId, String tokenHash, Instant now, Instant expiresAt, Instant familyExpiresAt) {
        RefreshToken token = new RefreshToken();
        token.user = user;
        token.familyId = familyId;
        token.tokenHash = tokenHash;
        token.createdAt = now;
        token.expiresAt = expiresAt;
        token.familyExpiresAt = familyExpiresAt;
        return token;
    }

    public boolean isExpiredAt(Instant instant) {
        return !instant.isBefore(expiresAt);
    }

    /** Revocado por una rotación (y no por logout o por robo detectado). */
    public boolean wasRotated() {
        return replacedById != null;
    }

    public void rotateTo(RefreshToken next, Instant now) {
        this.revokedAt = now;
        this.replacedById = next.getId();
    }
}
