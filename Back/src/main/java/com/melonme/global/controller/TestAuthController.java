package com.melonme.global.controller;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.JwtProvider;
import com.melonme.member.domain.Member;
import com.melonme.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Profile("local")
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> testLogin(@RequestParam Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        Map<String, Object> data = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "memberId", member.getId(),
                "role", member.getRole().name()
        );

        return ApiResponse.success(data);
    }
}
