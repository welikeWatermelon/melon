package com.melonme.admin.dto.response;

import com.melonme.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminMemberResponse {

    private Long id;
    private String nickname;
    private String therapyArea;
    private String role;
    private LocalDateTime createdAt;

    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getNickname(),
                member.getTherapyArea(),
                member.getRole().name(),
                member.getCreatedAt()
        );
    }
}
