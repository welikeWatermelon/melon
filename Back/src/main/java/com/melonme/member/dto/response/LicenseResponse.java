package com.melonme.member.dto.response;

import com.melonme.member.domain.License;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LicenseResponse {

    private Long id;
    private String status;
    private String adminMemo;
    private LocalDateTime createdAt;

    public static LicenseResponse from(License license) {
        return LicenseResponse.builder()
                .id(license.getId())
                .status(license.getStatus().name())
                .adminMemo(license.getAdminMemo())
                .createdAt(license.getCreatedAt())
                .build();
    }
}
