package com.healthybites.community.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findByGroupIdAndHiddenAtIsNull(UUID groupId, Pageable pageable);

    Optional<Post> findByIdAndHiddenAtIsNull(UUID id);
}
