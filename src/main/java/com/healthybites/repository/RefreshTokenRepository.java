package com.healthybites.repository;

import com.healthybites.model.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Bloquea la fila: dos refrescos simultáneos con el mismo token se serializan en lugar de rotar dos veces. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.usuario.id = :userId and t.revokedAt is null")
    int revokeAllForUser(@Param("userId") Integer userId, @Param("now") Instant now);

    @Modifying
    @Query("delete from RefreshToken t where t.familyExpiresAt < :threshold")
    int deleteExpiredBefore(@Param("threshold") Instant threshold);
}
