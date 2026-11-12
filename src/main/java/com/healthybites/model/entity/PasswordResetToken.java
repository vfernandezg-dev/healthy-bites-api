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

/**
 * Enlace para restablecer la contraseña. Como con los refresh tokens, solo se guarda el hash SHA-256 del valor:
 * quien lea la base de datos no puede usar los enlaces. Caduca pronto y sirve una sola vez.
 *
 * <p>Tabla nueva ({@code password_reset_tokens}): la anterior guardaba el token en claro y admitía uno por usuario.
 */
@Entity
@Table(name = "password_reset_tokens",
        indexes = @Index(name = "ix_password_reset_tokens_usuario", columnList = "usuario_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_password_reset_tokens_usuario"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    public static PasswordResetToken issue(Usuario usuario, String tokenHash, Instant now, Instant expiresAt) {
        PasswordResetToken token = new PasswordResetToken();
        token.usuario = usuario;
        token.tokenHash = tokenHash;
        token.createdAt = now;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean isUsable(Instant now) {
        return usedAt == null && now.isBefore(expiresAt);
    }

    public void markUsed(Instant now) {
        usedAt = now;
    }
}
