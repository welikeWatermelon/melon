package com.melonme.member.repository;

import com.melonme.member.domain.License;
import com.melonme.member.domain.LicenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    boolean existsByMemberIdAndStatus(Long memberId, LicenseStatus status);

    @Query("SELECT l FROM License l JOIN FETCH l.member WHERE l.status = :status ORDER BY l.createdAt DESC")
    Page<License> findByStatus(@Param("status") LicenseStatus status, Pageable pageable);
}
