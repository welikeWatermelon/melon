package com.melonme.member.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.global.security.JwtProvider;
import com.melonme.member.domain.Member;
import com.melonme.member.domain.Provider;
import com.melonme.member.domain.Role;
import com.melonme.member.dto.response.LoginResponse;
import com.melonme.member.dto.response.TokenRefreshResponse;
import com.melonme.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final OAuth2Service oAuth2Service;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse socialLogin(Provider provider, String code) {
        String[] userInfo = oAuth2Service.getUserInfo(provider, code);
        String providerId = userInfo[0];
        String nickname = userInfo[1];

        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> registerNewMember(provider, providerId, nickname));

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenService.save(member.getId(), refreshToken);

        return LoginResponse.of(accessToken, refreshToken, member);
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Long memberId = jwtProvider.getMemberIdFromToken(refreshToken);

        String storedToken = refreshTokenService.find(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // Rotate refresh token
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getId());
        refreshTokenService.save(memberId, newRefreshToken);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long memberId) {
        refreshTokenService.delete(memberId);
    }

    private Member registerNewMember(Provider provider, String providerId, String nickname) {
        // Ensure unique nickname by appending random suffix if duplicate
        String uniqueNickname = nickname;
        if (memberRepository.existsByNickname(nickname)) {
            uniqueNickname = nickname + "_" + UUID.randomUUID().toString().substring(0, 6);
        }

        Member member = Member.builder()
                .provider(provider)
                .providerId(providerId)
                .nickname(uniqueNickname)
                .role(Role.PENDING)
                .build();

        return memberRepository.save(member);
    }
}
