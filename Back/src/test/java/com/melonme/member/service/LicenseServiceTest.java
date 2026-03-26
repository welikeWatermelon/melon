package com.melonme.member.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.License;
import com.melonme.member.domain.LicenseStatus;
import com.melonme.member.domain.Member;
import com.melonme.member.domain.Provider;
import com.melonme.member.domain.Role;
import com.melonme.member.dto.response.LicenseResponse;
import com.melonme.member.repository.LicenseRepository;
import com.melonme.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @InjectMocks
    private LicenseService licenseService;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Client s3Client;

    @Test
    @DisplayName("면허 신청 중복 - 이미 PENDING 상태면 예외 발생")
    void applyLicense_alreadyPending_throwsException() {
        // given
        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .providerId("12345")
                .nickname("testUser")
                .role(Role.PENDING)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(licenseRepository.existsByMemberIdAndStatus(1L, LicenseStatus.PENDING)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> licenseService.applyLicense(1L, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.LICENSE_ALREADY_PENDING);
    }

    @Test
    @DisplayName("내 인증 상태 조회 - 신청 내역이 없으면 예외 발생")
    void getMyLicense_notFound_throwsException() {
        // given
        given(licenseRepository.findTopByMemberIdOrderByCreatedAtDesc(1L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> licenseService.getMyLicense(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.LICENSE_NOT_FOUND);
    }

    @Test
    @DisplayName("내 인증 상태 조회 - 정상적으로 조회된다")
    void getMyLicense_success() {
        // given
        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .providerId("12345")
                .nickname("testUser")
                .role(Role.PENDING)
                .build();

        License license = License.builder()
                .member(member)
                .licenseImgUrl("https://s3.amazonaws.com/test.jpg")
                .build();

        given(licenseRepository.findTopByMemberIdOrderByCreatedAtDesc(1L))
                .willReturn(Optional.of(license));

        // when
        LicenseResponse response = licenseService.getMyLicense(1L);

        // then
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }
}
