package com.melonme.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LicenseReviewRequest {

    @NotNull(message = "처리 상태를 입력해야 합니다.")
    private String status; // APPROVED, REJECTED

    private String adminMemo;
}
