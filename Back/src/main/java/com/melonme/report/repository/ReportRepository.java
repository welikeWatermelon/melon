package com.melonme.report.repository;

import com.melonme.report.domain.Report;
import com.melonme.report.domain.ReportStatus;
import com.melonme.report.domain.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, TargetType targetType, Long targetId);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
}
