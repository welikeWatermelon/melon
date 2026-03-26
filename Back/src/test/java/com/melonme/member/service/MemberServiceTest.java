package com.melonme.member.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.Member;
import com.melonme.member.domain.Provider;
import com.melonme.member.domain.Role;
import com.melonme.member.dto.request.MemberUpdateRequest;
import com.melonme.member.dto.response.MemberInfoResponse;
import com.melonme.member.dto.response.MemberUpdateResponse;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private Member createTestMember() {
        return Member.builder()
                .provider(Provider.KAKAO)
                .providerId("12345")
                .nickname("testUser")
                .therapyArea("언어")
                .role(Role.MEMBER)
                .build();
    }

    @Test
    @DisplayName("내 정보 조회 - 정상적으로 조회된다")
    void getMyInfo_success() {
        // given
        Member member = createTestMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        MemberInfoResponse response = memberService.getMyInfo(1L);

        // then
        assertThat(response.getNickname()).isEqualTo("testUser");
        assertThat(response.getTherapyArea()).isEqualTo("언어");
        assertThat(response.getRole()).isEqualTo("MEMBER");
    }

    @Test
    @DisplayName("내 정보 조회 - 존재하지 않는 회원이면 예외 발생")
    void getMyInfo_memberNotFound_throwsException() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMyInfo(999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("닉네임 수정 - 중복 닉네임이면 예외 발생")
    void updateMyInfo_duplicateNickname_throwsException() {
        // given
        Member member = createTestMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByNickname("duplicatedNick")).willReturn(true);

        MemberUpdateRequest request = new MemberUpdateRequest();
        setField(request, "nickname", "duplicatedNick");

        // when & then
        assertThatThrownBy(() -> memberService.updateMyInfo(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_NICKNAME_DUPLICATE);
    }

    @Test
    @DisplayName("닉네임 수정 - 새 닉네임이 사용가능하면 성공한다")
    void updateMyInfo_validNickname_success() {
        // given
        Member member = createTestMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(memberRepository.existsByNickname("newNickname")).willReturn(false);

        MemberUpdateRequest request = new MemberUpdateRequest();
        setField(request, "nickname", "newNickname");
        setField(request, "therapyArea", "작업");

        // when
        MemberUpdateResponse response = memberService.updateMyInfo(1L, request);

        // then
        assertThat(response.getNickname()).isEqualTo("newNickname");
        assertThat(response.getTherapyArea()).isEqualTo("작업");
    }

    @Test
    @DisplayName("회원 탈퇴 - softDelete 수행 후 RefreshToken 삭제")
    void deleteMe_success() {
        // given
        Member member = createTestMember();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        memberService.deleteMe(1L);

        // then
        assertThat(member.isDeleted()).isTrue();
        verify(refreshTokenService).delete(1L);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
