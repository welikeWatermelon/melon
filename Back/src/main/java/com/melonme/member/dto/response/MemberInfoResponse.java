package com.melonme.member.dto.response;

import com.melonme.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfoResponse {

    private Long id;
    private String nickname;
    private String therapyArea;
    private String role;

    public static MemberInfoResponse from(Member member) {
        return MemberInfoResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .therapyArea(member.getTherapyArea())
                .role(member.getRole().name())
                .build();
    }
}
