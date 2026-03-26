package com.melonme.member.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.Member;
import com.melonme.member.dto.request.MemberUpdateRequest;
import com.melonme.member.dto.response.MemberInfoResponse;
import com.melonme.member.dto.response.MemberUpdateResponse;
import com.melonme.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public MemberInfoResponse getMyInfo(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberInfoResponse.from(member);
    }

    @Transactional
    public MemberUpdateResponse updateMyInfo(Long memberId, MemberUpdateRequest request) {
        Member member = findMemberById(memberId);

        if (request.getNickname() != null && !request.getNickname().equals(member.getNickname())) {
            if (memberRepository.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.MEMBER_NICKNAME_DUPLICATE);
            }
        }

        member.updateProfile(request.getNickname(), request.getTherapyArea());
        return MemberUpdateResponse.from(member);
    }

    @Transactional
    public void deleteMe(Long memberId) {
        Member member = findMemberById(memberId);
        member.softDelete();
        refreshTokenService.delete(memberId);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
