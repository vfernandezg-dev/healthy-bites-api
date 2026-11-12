package com.healthybites.repository;

import com.healthybites.model.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    Optional<MediaFile> findByFilename(String filename);
}
