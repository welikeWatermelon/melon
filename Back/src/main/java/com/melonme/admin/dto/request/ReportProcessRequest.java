package com.melonme.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportProcessRequest {

    @NotNull(message = "처리 액션을 입력해야 합니다.")
    private String action; // HIDE_POST, HIDE_COMMENT, DISMISS
}
