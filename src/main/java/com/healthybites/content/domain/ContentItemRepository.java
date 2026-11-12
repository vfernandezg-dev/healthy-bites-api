package com.healthybites.content.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID>, JpaSpecificationExecutor<ContentItem> {

    Optional<ContentItem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<ContentItem> findByIdAndAuthorId(UUID id, UUID authorId);

    Page<ContentItem> findByAuthorId(UUID authorId, Pageable pageable);

    @Query("select count(c) > 0 from ContentItem c where c.id = :id and c.authorId = :authorId"
            + " and c.status = com.healthybites.content.domain.ContentEnums.ContentStatus.PUBLISHED")
    boolean isPublishedBy(@Param("id") UUID id, @Param("authorId") UUID authorId);
}
