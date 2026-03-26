package com.melonme.member.dto.response;

import com.melonme.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberUpdateResponse {

    private Long id;
    private String nickname;
    private String therapyArea;

    public static MemberUpdateResponse from(Member member) {
        return MemberUpdateResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .therapyArea(member.getTherapyArea())
                .build();
    }
}
