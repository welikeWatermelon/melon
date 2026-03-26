package com.melonme.report.dto.request;

import com.melonme.report.domain.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {

    @NotNull(message = "신고 대상 유형은 필수입니다.")
    private TargetType targetType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;

    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason;

    public ReportCreateRequest(TargetType targetType, Long targetId, String reason) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }
}
