package com.melonme.admin.dto.response;

import com.melonme.report.domain.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminReportResponse {

    private Long id;
    private Long reporterId;
    private String targetType;
    private Long targetId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public static AdminReportResponse from(Report report) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus().name(),
                report.getCreatedAt()
        );
    }
}
