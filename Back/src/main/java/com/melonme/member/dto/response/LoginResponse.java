package com.melonme.member.dto.response;

import com.melonme.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private MemberInfoResponse member;

    public static LoginResponse of(String accessToken, String refreshToken, Member member) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberInfoResponse.from(member))
                .build();
    }
}
