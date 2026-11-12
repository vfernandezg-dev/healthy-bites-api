package com.healthybites.content.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentUnlockRepository extends JpaRepository<ContentUnlock, UUID> {

    Optional<ContentUnlock> findByContentItemIdAndUserId(UUID contentItemId, UUID userId);

    boolean existsByContentItemIdAndUserId(UUID contentItemId, UUID userId);

    List<ContentUnlock> findByUserIdOrderByUnlockedAtDesc(UUID userId);

    @Query("select u.contentItemId from ContentUnlock u where u.userId = :userId")
    Set<UUID> unlockedIds(@Param("userId") UUID userId);
}
