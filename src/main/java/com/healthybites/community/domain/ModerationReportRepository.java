package com.healthybites.community.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationReportRepository extends JpaRepository<ModerationReport, UUID> {

    Page<ModerationReport> findByStatus(ModerationReport.Status status, Pageable pageable);

    boolean existsByReporterIdAndPostIdAndStatus(UUID reporterId, UUID postId, ModerationReport.Status status);

    boolean existsByReporterIdAndCommentIdAndStatus(UUID reporterId, UUID commentId, ModerationReport.Status status);

    List<ModerationReport> findByPostIdAndStatus(UUID postId, ModerationReport.Status status);

    List<ModerationReport> findByCommentIdAndStatus(UUID commentId, ModerationReport.Status status);
}
