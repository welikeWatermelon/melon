package com.melonme.file.repository;

import com.melonme.file.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT f FROM FileEntity f WHERE f.deletedAt IS NOT NULL")
    List<FileEntity> findAllSoftDeleted();

    List<FileEntity> findAllByPostId(Long postId);
}
