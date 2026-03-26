package com.melonme.admin.service;

import com.melonme.admin.domain.MemberAction;
import com.melonme.admin.domain.ReportAction;
import com.melonme.admin.dto.request.LicenseReviewRequest;
import com.melonme.admin.dto.request.MemberActionRequest;
import com.melonme.admin.dto.request.ReportProcessRequest;
import com.melonme.admin.dto.response.AdminLicenseResponse;
import com.melonme.admin.dto.response.AdminMemberResponse;
import com.melonme.admin.dto.response.AdminReportResponse;
import com.melonme.admin.dto.response.AdminStatsResponse;
import com.melonme.admin.repository.AdminStatsRepository;
import com.melonme.comment.domain.Comment;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.License;
import com.melonme.member.domain.LicenseStatus;
import com.melonme.member.domain.Member;
import com.melonme.member.domain.Role;
import com.melonme.member.repository.LicenseRepository;
import com.melonme.member.repository.MemberRepository;
import com.melonme.post.domain.Post;
import com.melonme.post.repository.PostRepository;
import com.melonme.report.domain.Report;
import com.melonme.report.domain.ReportStatus;
import com.melonme.report.domain.TargetType;
import com.melonme.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final LicenseRepository licenseRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AdminStatsRepository adminStatsRepository;

    // ======================== 면허 인증 ========================

    @Transactional(readOnly = true)
    public Page<AdminLicenseResponse> getLicenses(String status, Pageable pageable) {
        Page<License> licenses;
        if (status != null) {
            LicenseStatus licenseStatus = LicenseStatus.valueOf(status);
            licenses = licenseRepository.findByStatus(licenseStatus, pageable);
        } else {
            licenses = licenseRepository.findAll(pageable);
        }
        return licenses.map(AdminLicenseResponse::from);
    }

    @Transactional
    public void reviewLicense(Long licenseId, Long adminId, LicenseReviewRequest request) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_LICENSE_NOT_FOUND));

        if (license.getStatus() != LicenseStatus.PENDING) {
            throw new CustomException(ErrorCode.ADMIN_LICENSE_ALREADY_REVIEWED);
        }

        LicenseStatus newStatus = LicenseStatus.valueOf(request.getStatus());

        if (newStatus == LicenseStatus.APPROVED) {
            license.approve(adminId);
            Member member = license.getMember();
            member.updateRole(Role.MEMBER);
        } else if (newStatus == LicenseStatus.REJECTED) {
            if (request.getAdminMemo() == null || request.getAdminMemo().isBlank()) {
                throw new CustomException(ErrorCode.ADMIN_REJECT_MEMO_REQUIRED);
            }
            license.reject(adminId, request.getAdminMemo());
        } else {
            throw new CustomException(ErrorCode.ADMIN_INVALID_ACTION);
        }
    }

    // ======================== 신고 ========================

    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getReports(String status, Pageable pageable) {
        Page<Report> reports;
        if (status != null) {
            ReportStatus reportStatus = ReportStatus.valueOf(status);
            reports = reportRepository.findByStatus(reportStatus, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        return reports.map(AdminReportResponse::from);
    }

    @Transactional
    public void processReport(Long reportId, ReportProcessRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_REPORT_NOT_FOUND));

        if (report.getStatus() == ReportStatus.PROCESSED) {
            throw new CustomException(ErrorCode.ADMIN_REPORT_ALREADY_PROCESSED);
        }

        ReportAction action = ReportAction.valueOf(request.getAction());

        switch (action) {
            case HIDE_POST -> {
                if (report.getTargetType() != TargetType.POST) {
                    throw new CustomException(ErrorCode.ADMIN_INVALID_ACTION);
                }
                Post post = postRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_TARGET_NOT_FOUND));
                post.hide();
            }
            case HIDE_COMMENT -> {
                if (report.getTargetType() != TargetType.COMMENT) {
                    throw new CustomException(ErrorCode.ADMIN_INVALID_ACTION);
                }
                Comment comment = commentRepository.findById(report.getTargetId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_TARGET_NOT_FOUND));
                comment.hideByAdmin();
            }
            case DISMISS -> {
                // 기각: 별도 액션 없음
            }
        }

        report.process();
    }

    // ======================== 회원 ========================

    @Transactional(readOnly = true)
    public Page<AdminMemberResponse> getMembers(String role, Pageable pageable) {
        Page<Member> members;
        if (role != null) {
            Role memberRole = Role.valueOf(role);
            members = memberRepository.findByRole(memberRole, pageable);
        } else {
            members = memberRepository.findAll(pageable);
        }
        return members.map(AdminMemberResponse::from);
    }

    @Transactional
    public void actionMember(Long memberId, MemberActionRequest request) {
        MemberAction action = MemberAction.valueOf(request.getAction());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (action == MemberAction.FORCE_DELETE) {
            member.softDelete();
        }
    }

    // ======================== 통계 ========================

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long wau = adminStatsRepository.countActiveUsersSince(sevenDaysAgo);
        long mau = adminStatsRepository.countActiveUsersSince(thirtyDaysAgo);
        long weeklyPostCount = adminStatsRepository.countPostsSince(sevenDaysAgo);

        long memberRoleCount = adminStatsRepository.countMemberRoleMembers();
        long uniqueUploaders = adminStatsRepository.countUniquePostAuthorsSince(thirtyDaysAgo);
        double uploaderRatio = memberRoleCount > 0
                ? Math.round((double) uniqueUploaders / memberRoleCount * 1000.0) / 10.0
                : 0.0;

        // 30일 잔존율: 30일 전 가입 회원 중 최근 7일 활동한 비율
        long joinedBefore30Days = adminStatsRepository.countMembersJoinedBefore(thirtyDaysAgo);
        long retained = adminStatsRepository.countRetainedMembers(thirtyDaysAgo, sevenDaysAgo);
        double retentionRate = joinedBefore30Days > 0
                ? Math.round((double) retained / joinedBefore30Days * 1000.0) / 10.0
                : 0.0;

        return AdminStatsResponse.builder()
                .wau(wau)
                .mau(mau)
                .weeklyPostCount(weeklyPostCount)
                .uploaderRatio(uploaderRatio)
                .retentionRate(retentionRate)
                .build();
    }
}
