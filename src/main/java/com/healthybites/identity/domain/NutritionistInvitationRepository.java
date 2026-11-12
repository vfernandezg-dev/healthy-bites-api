package com.healthybites.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NutritionistInvitationRepository extends JpaRepository<NutritionistInvitation, UUID> {

    Optional<NutritionistInvitation> findByTokenHash(String tokenHash);

    /** ¿Hay ya una invitación pendiente para ese correo? */
    @Query("""
            select count(i) > 0 from NutritionistInvitation i
            where lower(i.email) = lower(:email) and i.acceptedAt is null and i.revokedAt is null
              and i.expiresAt > :now""")
    boolean existsPendingFor(@Param("email") String email, @Param("now") Instant now);

    Page<NutritionistInvitation> findAllBy(Pageable pageable);
}
