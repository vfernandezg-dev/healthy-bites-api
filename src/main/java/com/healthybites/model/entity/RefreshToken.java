package com.healthybites.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token de una sesión. Solo se guarda el hash SHA-256 del valor: quien lea la base de datos no puede
 * usar los tokens. Cada rotación crea un token nuevo en la misma familia (la sesión) y revoca el anterior.
 */
@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "ix_refresh_tokens_family", columnList = "family_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_tokens_usuario"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

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
    private Instant revokedAt;

    @Column(name = "replaced_by_id")
    private Long replacedById;

    public static RefreshToken issue(Usuario usuario, UUID familyId, String tokenHash, Instant now,
                                     Instant expiresAt, Instant familyExpiresAt) {
        RefreshToken token = new RefreshToken();
        token.usuario = usuario;
        token.familyId = familyId;
        token.tokenHash = tokenHash;
        token.createdAt = now;
        token.expiresAt = expiresAt;
        token.familyExpiresAt = familyExpiresAt;
        return token;
    }

    public boolean isRevoked() {
        return revokedAt != null;
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
