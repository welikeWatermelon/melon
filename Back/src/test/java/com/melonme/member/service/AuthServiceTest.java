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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OAuth2Service oAuth2Service;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("소셜 로그인 - 신규 회원가입 시 PENDING role로 생성된다")
    void socialLogin_newMember_createWithPendingRole() {
        // given
        String code = "auth-code";
        given(oAuth2Service.getUserInfo(Provider.KAKAO, code))
                .willReturn(new String[]{"12345", "testUser"});
        given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "12345"))
                .willReturn(Optional.empty());
        given(memberRepository.existsByNickname("testUser")).willReturn(false);

        Member savedMember = Member.builder()
                .provider(Provider.KAKAO)
                .providerId("12345")
                .nickname("testUser")
                .role(Role.PENDING)
                .build();

        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(jwtProvider.createAccessToken(any(), eq("PENDING"))).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("refresh-token");

        // when
        LoginResponse response = authService.socialLogin(Provider.KAKAO, code);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getMember().getRole()).isEqualTo("PENDING");
        verify(refreshTokenService).save(any(), eq("refresh-token"));
    }

    @Test
    @DisplayName("소셜 로그인 - 기존 회원은 새로 생성하지 않고 토큰만 발급한다")
    void socialLogin_existingMember_returnsTokenWithoutCreating() {
        // given
        String code = "auth-code";
        given(oAuth2Service.getUserInfo(Provider.KAKAO, code))
                .willReturn(new String[]{"12345", "testUser"});

        Member existingMember = Member.builder()
                .provider(Provider.KAKAO)
                .providerId("12345")
                .nickname("testUser")
                .role(Role.MEMBER)
                .build();

        given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "12345"))
                .willReturn(Optional.of(existingMember));
        given(jwtProvider.createAccessToken(any(), eq("MEMBER"))).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("refresh-token");

        // when
        LoginResponse response = authService.socialLogin(Provider.KAKAO, code);

        // then
        assertThat(response.getMember().getRole()).isEqualTo("MEMBER");
    }

    @Test
    @DisplayName("토큰 재발급 - RefreshToken rotate가 수행된다")
    void refresh_validToken_rotatesRefreshToken() {
        // given
        String oldRefreshToken = "old-refresh-token";
        given(jwtProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtProvider.getMemberIdFromToken(oldRefreshToken)).willReturn(1L);
        given(refreshTokenService.find(1L)).willReturn(Optional.of(oldRefreshToken));

        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .providerId("12345")
                .nickname("testUser")
                .role(Role.MEMBER)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(jwtProvider.createAccessToken(any(), eq("MEMBER"))).willReturn("new-access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("new-refresh-token");

        // when
        TokenRefreshResponse response = authService.refresh(oldRefreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        verify(refreshTokenService).save(eq(1L), eq("new-refresh-token"));
    }

    @Test
    @DisplayName("토큰 재발급 - 유효하지 않은 토큰이면 예외 발생")
    void refresh_invalidToken_throwsException() {
        // given
        String invalidToken = "invalid-token";
        given(jwtProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refresh(invalidToken))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("토큰 재발급 - Redis에 저장된 토큰과 다르면 예외 발생")
    void refresh_tokenMismatch_throwsException() {
        // given
        String refreshToken = "refresh-token";
        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getMemberIdFromToken(refreshToken)).willReturn(1L);
        given(refreshTokenService.find(1L)).willReturn(Optional.of("different-token"));

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("로그아웃 - Redis에서 RefreshToken을 삭제한다")
    void logout_deletesRefreshToken() {
        // given
        Long memberId = 1L;

        // when
        authService.logout(memberId);

        // then
        verify(refreshTokenService).delete(memberId);
    }
}
