package com.healthybites.content.domain;

import com.healthybites.shared.domain.PublicationStatus;
import java.util.List;
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

    Page<ContentItem> findByAuthorIdAndStatus(UUID authorId, PublicationStatus status, Pageable pageable);

    @Query(
            "select c.status as status, count(c) as total from ContentItem c where c.authorId = :authorId group by c.status")
    List<StatusCount> countByStatus(@Param("authorId") UUID authorId);

    interface StatusCount {
        PublicationStatus getStatus();

        long getTotal();
    }

    @Query("select count(c) > 0 from ContentItem c where c.id = :id and c.authorId = :authorId"
            + " and c.status = com.healthybites.shared.domain.PublicationStatus.PUBLISHED")
    boolean isPublishedBy(@Param("id") UUID id, @Param("authorId") UUID authorId);
}
