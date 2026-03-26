package com.melonme.admin.dto.response;

import com.melonme.member.domain.License;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminLicenseResponse {

    private Long id;
    private Long memberId;
    private String nickname;
    private String licenseImgUrl;
    private String status;
    private LocalDateTime createdAt;

    public static AdminLicenseResponse from(License license) {
        return new AdminLicenseResponse(
                license.getId(),
                license.getMember().getId(),
                license.getMember().getNickname(),
                license.getLicenseImgUrl(),
                license.getStatus().name(),
                license.getCreatedAt()
        );
    }
}
