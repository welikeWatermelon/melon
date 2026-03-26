package com.melonme.like.dto.request;

import com.melonme.like.domain.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LikeToggleRequest {

    @NotNull(message = "대상 타입은 필수입니다.")
    private TargetType targetType;

    @NotNull(message = "대상 ID는 필수입니다.")
    private Long targetId;

    public LikeToggleRequest(TargetType targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
