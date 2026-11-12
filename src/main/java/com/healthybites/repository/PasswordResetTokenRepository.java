package com.healthybites.repository;

import com.healthybites.model.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Invalida los enlaces anteriores del usuario: solo vale el último que se pidió. */
    @Modifying
    @Query("delete from PasswordResetToken t where t.usuario.id = :usuarioId")
    int deleteAllOfUser(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :before")
    int deleteExpiredBefore(@Param("before") Instant before);
}
