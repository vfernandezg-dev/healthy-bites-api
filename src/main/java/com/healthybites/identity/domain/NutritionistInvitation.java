package com.healthybites.identity.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Invitación de un ADMIN para que un nutricionista cree su cuenta. Del token solo se guarda el hash; reenviarla
 * genera un token nuevo (el anterior deja de valer) y amplía la caducidad.
 */
@Entity
@Table(name = "nutritionist_invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NutritionistInvitation extends BaseEntity {

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private @Nullable Instant acceptedAt;

    @Column(name = "accepted_user_id")
    private @Nullable UUID acceptedUserId;

    @Column(name = "revoked_at")
    private @Nullable Instant revokedAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        EXPIRED,
        REVOKED
    }

    public static NutritionistInvitation issue(
            String email, String firstName, String lastName, String tokenHash, UUID invitedBy, Instant expiresAt) {
        NutritionistInvitation invitation = new NutritionistInvitation();
        invitation.email = User.normalizeEmail(email);
        invitation.firstName = firstName.trim();
        invitation.lastName = lastName.trim();
        invitation.tokenHash = tokenHash;
        invitation.invitedBy = invitedBy;
        invitation.expiresAt = expiresAt;
        return invitation;
    }

    public Status statusAt(Instant now) {
        if (acceptedAt != null) {
            return Status.ACCEPTED;
        }
        if (revokedAt != null) {
            return Status.REVOKED;
        }
        return now.isBefore(expiresAt) ? Status.PENDING : Status.EXPIRED;
    }

    public boolean isUsableAt(Instant now) {
        return statusAt(now) == Status.PENDING;
    }

    /** Token nuevo y caducidad ampliada (el enlace anterior deja de valer). */
    public void reissue(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public void accept(UUID userId, Instant now) {
        this.acceptedAt = now;
        this.acceptedUserId = userId;
    }

    public void revoke(Instant now) {
        this.revokedAt = now;
    }
}
