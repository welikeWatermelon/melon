package com.melonme.admin.service;

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
import com.melonme.comment.domain.CommentStatus;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.License;
import com.melonme.member.domain.LicenseStatus;
import com.melonme.member.domain.Member;
import com.melonme.member.domain.Provider;
import com.melonme.member.domain.Role;
import com.melonme.member.repository.LicenseRepository;
import com.melonme.member.repository.MemberRepository;
import com.melonme.post.domain.Post;
import com.melonme.post.domain.PostStatus;
import com.melonme.post.repository.PostRepository;
import com.melonme.report.domain.Report;
import com.melonme.report.domain.ReportStatus;
import com.melonme.report.domain.TargetType;
import com.melonme.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private LicenseRepository licenseRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AdminStatsRepository adminStatsRepository;

    // ======================== Helper ========================

    private Member createMember(Long id, Role role) {
        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .providerId("kakao_" + id)
                .nickname("user" + id)
                .therapyArea("작업")
                .role(role)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private License createLicense(Long id, Member member, LicenseStatus status) {
        License license = License.builder()
                .member(member)
                .licenseImgUrl("https://s3.example.com/license.jpg")
                .build();
        ReflectionTestUtils.setField(license, "id", id);
        if (status == LicenseStatus.APPROVED) {
            license.approve(1L);
        } else if (status == LicenseStatus.REJECTED) {
            license.reject(1L, "사유");
        }
        return license;
    }

    private Report createReport(Long id, TargetType targetType, Long targetId, ReportStatus status) {
        Report report = Report.builder()
                .reporterId(99L)
                .targetType(targetType)
                .targetId(targetId)
                .reason("부적절한 내용")
                .build();
        ReflectionTestUtils.setField(report, "id", id);
        if (status == ReportStatus.PROCESSED) {
            report.process();
        }
        return report;
    }

    private Post createPost(Long id) {
        Post post = Post.builder()
                .memberId(10L)
                .title("테스트 게시글")
                .content("{}")
                .therapyArea("작업")
                .isAnonymous(false)
                .build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Comment createComment(Long id) {
        Comment comment = Comment.builder()
                .postId(1L)
                .memberId(10L)
                .content("테스트 댓글")
                .isAnonymous(false)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    private LicenseReviewRequest createLicenseReviewRequest(String status, String adminMemo) {
        LicenseReviewRequest request = new LicenseReviewRequest();
        ReflectionTestUtils.setField(request, "status", status);
        ReflectionTestUtils.setField(request, "adminMemo", adminMemo);
        return request;
    }

    private ReportProcessRequest createReportProcessRequest(String action) {
        ReportProcessRequest request = new ReportProcessRequest();
        ReflectionTestUtils.setField(request, "action", action);
        return request;
    }

    private MemberActionRequest createMemberActionRequest(String action) {
        MemberActionRequest request = new MemberActionRequest();
        ReflectionTestUtils.setField(request, "action", action);
        return request;
    }

    // ======================== 면허 인증 테스트 ========================

    @Nested
    @DisplayName("면허 인증 관리")
    class LicenseTest {

        @Test
        @DisplayName("면허 인증 목록 조회 - status 필터")
        void getLicenses_withStatusFilter() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Member member = createMember(1L, Role.PENDING);
            License license = createLicense(1L, member, LicenseStatus.PENDING);
            Page<License> licensePage = new PageImpl<>(List.of(license));
            given(licenseRepository.findByStatus(eq(LicenseStatus.PENDING), any(Pageable.class)))
                    .willReturn(licensePage);

            // when
            Page<AdminLicenseResponse> result = adminService.getLicenses("PENDING", pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("면허 인증 목록 조회 - 전체")
        void getLicenses_all() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Member member = createMember(1L, Role.PENDING);
            License license = createLicense(1L, member, LicenseStatus.PENDING);
            Page<License> licensePage = new PageImpl<>(List.of(license));
            given(licenseRepository.findAll(any(Pageable.class))).willReturn(licensePage);

            // when
            Page<AdminLicenseResponse> result = adminService.getLicenses(null, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("면허 승인 시 Member.role = MEMBER로 변경")
        void approveLicense_changesRole() {
            // given
            Member member = createMember(1L, Role.PENDING);
            License license = createLicense(1L, member, LicenseStatus.PENDING);
            given(licenseRepository.findById(1L)).willReturn(Optional.of(license));
            LicenseReviewRequest request = createLicenseReviewRequest("APPROVED", null);

            // when
            adminService.reviewLicense(1L, 99L, request);

            // then
            assertThat(license.getStatus()).isEqualTo(LicenseStatus.APPROVED);
            assertThat(member.getRole()).isEqualTo(Role.MEMBER);
        }

        @Test
        @DisplayName("면허 거절 시 adminMemo 필수")
        void rejectLicense_requiresMemo() {
            // given
            Member member = createMember(1L, Role.PENDING);
            License license = createLicense(1L, member, LicenseStatus.PENDING);
            given(licenseRepository.findById(1L)).willReturn(Optional.of(license));
            LicenseReviewRequest request = createLicenseReviewRequest("REJECTED", null);

            // when & then
            assertThatThrownBy(() -> adminService.reviewLicense(1L, 99L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_REJECT_MEMO_REQUIRED);
        }

        @Test
        @DisplayName("면허 거절 - 빈 문자열 memo도 거절")
        void rejectLicense_blankMemoFails() {
            // given
            Member member = createMember(1L, Role.PENDING);
            License license = createLicense(1L, member, LicenseStatus.PENDING);
            given(licenseRepository.findById(1L)).willReturn(Optional.of(license));
            LicenseReviewRequest request = createLicenseReviewRequest("REJECTED", "   ");

            // when & then
            assertThatThrownBy(() -> adminService.reviewLicense(1L, 99L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_REJECT_MEMO_REQUIRED);
        }

        @Test
        @DisplayName("면허 거절 성공 - adminMemo 포함")
        void rejectLicense_success() {
            // given
            Member member = createMember(1L, Role.PENDING);
            License license = createLicense(1L, member, LicenseStatus.PENDING);
            given(licenseRepository.findById(1L)).willReturn(Optional.of(license));
            LicenseReviewRequest request = createLicenseReviewRequest("REJECTED", "면허증이 불분명합니다.");

            // when
            adminService.reviewLicense(1L, 99L, request);

            // then
            assertThat(license.getStatus()).isEqualTo(LicenseStatus.REJECTED);
            assertThat(license.getAdminMemo()).isEqualTo("면허증이 불분명합니다.");
        }

        @Test
        @DisplayName("이미 처리된 면허 신청 재처리 시 예외")
        void reviewLicense_alreadyReviewed() {
            // given
            Member member = createMember(1L, Role.MEMBER);
            License license = createLicense(1L, member, LicenseStatus.APPROVED);
            given(licenseRepository.findById(1L)).willReturn(Optional.of(license));
            LicenseReviewRequest request = createLicenseReviewRequest("APPROVED", null);

            // when & then
            assertThatThrownBy(() -> adminService.reviewLicense(1L, 99L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_LICENSE_ALREADY_REVIEWED);
        }

        @Test
        @DisplayName("존재하지 않는 면허 신청 처리 시 예외")
        void reviewLicense_notFound() {
            // given
            given(licenseRepository.findById(999L)).willReturn(Optional.empty());
            LicenseReviewRequest request = createLicenseReviewRequest("APPROVED", null);

            // when & then
            assertThatThrownBy(() -> adminService.reviewLicense(999L, 99L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_LICENSE_NOT_FOUND);
        }
    }

    // ======================== 신고 처리 테스트 ========================

    @Nested
    @DisplayName("신고 처리")
    class ReportTest {

        @Test
        @DisplayName("신고 목록 조회 - status 필터")
        void getReports_withStatusFilter() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Report report = createReport(1L, TargetType.POST, 1L, ReportStatus.PENDING);
            Page<Report> reportPage = new PageImpl<>(List.of(report));
            given(reportRepository.findByStatus(eq(ReportStatus.PENDING), any(Pageable.class)))
                    .willReturn(reportPage);

            // when
            Page<AdminReportResponse> result = adminService.getReports("PENDING", pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("HIDE_POST 액션 - 게시글 상태 HIDDEN으로 변경")
        void processReport_hidePost() {
            // given
            Report report = createReport(1L, TargetType.POST, 10L, ReportStatus.PENDING);
            Post post = createPost(10L);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            ReportProcessRequest request = createReportProcessRequest("HIDE_POST");

            // when
            adminService.processReport(1L, request);

            // then
            assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
            assertThat(report.getStatus()).isEqualTo(ReportStatus.PROCESSED);
        }

        @Test
        @DisplayName("HIDE_COMMENT 액션 - 댓글 상태 DELETED로 변경")
        void processReport_hideComment() {
            // given
            Report report = createReport(1L, TargetType.COMMENT, 20L, ReportStatus.PENDING);
            Comment comment = createComment(20L);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            given(commentRepository.findById(20L)).willReturn(Optional.of(comment));
            ReportProcessRequest request = createReportProcessRequest("HIDE_COMMENT");

            // when
            adminService.processReport(1L, request);

            // then
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
            assertThat(report.getStatus()).isEqualTo(ReportStatus.PROCESSED);
        }

        @Test
        @DisplayName("DISMISS 액션 - 신고 기각")
        void processReport_dismiss() {
            // given
            Report report = createReport(1L, TargetType.POST, 10L, ReportStatus.PENDING);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = createReportProcessRequest("DISMISS");

            // when
            adminService.processReport(1L, request);

            // then
            assertThat(report.getStatus()).isEqualTo(ReportStatus.PROCESSED);
        }

        @Test
        @DisplayName("HIDE_POST를 COMMENT 신고에 적용 시 예외")
        void processReport_hidePost_onCommentReport() {
            // given
            Report report = createReport(1L, TargetType.COMMENT, 20L, ReportStatus.PENDING);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = createReportProcessRequest("HIDE_POST");

            // when & then
            assertThatThrownBy(() -> adminService.processReport(1L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_INVALID_ACTION);
        }

        @Test
        @DisplayName("HIDE_COMMENT를 POST 신고에 적용 시 예외")
        void processReport_hideComment_onPostReport() {
            // given
            Report report = createReport(1L, TargetType.POST, 10L, ReportStatus.PENDING);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = createReportProcessRequest("HIDE_COMMENT");

            // when & then
            assertThatThrownBy(() -> adminService.processReport(1L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_INVALID_ACTION);
        }

        @Test
        @DisplayName("이미 처리된 신고 재처리 시 예외")
        void processReport_alreadyProcessed() {
            // given
            Report report = createReport(1L, TargetType.POST, 10L, ReportStatus.PROCESSED);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = createReportProcessRequest("DISMISS");

            // when & then
            assertThatThrownBy(() -> adminService.processReport(1L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_REPORT_ALREADY_PROCESSED);
        }

        @Test
        @DisplayName("존재하지 않는 신고 처리 시 예외")
        void processReport_notFound() {
            // given
            given(reportRepository.findById(999L)).willReturn(Optional.empty());
            ReportProcessRequest request = createReportProcessRequest("DISMISS");

            // when & then
            assertThatThrownBy(() -> adminService.processReport(999L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ADMIN_REPORT_NOT_FOUND);
        }
    }

    // ======================== 회원 관리 테스트 ========================

    @Nested
    @DisplayName("회원 관리")
    class MemberTest {

        @Test
        @DisplayName("회원 목록 조회 - role 필터")
        void getMembers_withRoleFilter() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Member member = createMember(1L, Role.MEMBER);
            Page<Member> memberPage = new PageImpl<>(List.of(member));
            given(memberRepository.findByRole(eq(Role.MEMBER), any(Pageable.class)))
                    .willReturn(memberPage);

            // when
            Page<AdminMemberResponse> result = adminService.getMembers("MEMBER", pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getRole()).isEqualTo("MEMBER");
        }

        @Test
        @DisplayName("회원 강제 탈퇴 - soft delete")
        void forceDeleteMember() {
            // given
            Member member = createMember(1L, Role.MEMBER);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            MemberActionRequest request = createMemberActionRequest("FORCE_DELETE");

            // when
            adminService.actionMember(1L, request);

            // then
            assertThat(member.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 회원 강제 탈퇴 시 예외")
        void forceDeleteMember_notFound() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());
            MemberActionRequest request = createMemberActionRequest("FORCE_DELETE");

            // when & then
            assertThatThrownBy(() -> adminService.actionMember(999L, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    // ======================== 통계 테스트 ========================

    @Nested
    @DisplayName("통계 조회")
    class StatsTest {

        @Test
        @DisplayName("통계 정상 조회")
        void getStats_success() {
            // given
            given(adminStatsRepository.countActiveUsersSince(any(LocalDateTime.class)))
                    .willReturn(100L)   // WAU (7일)
                    .willReturn(500L);  // MAU (30일)
            given(adminStatsRepository.countPostsSince(any(LocalDateTime.class)))
                    .willReturn(50L);
            given(adminStatsRepository.countMemberRoleMembers())
                    .willReturn(200L);
            given(adminStatsRepository.countUniquePostAuthorsSince(any(LocalDateTime.class)))
                    .willReturn(40L);
            given(adminStatsRepository.countMembersJoinedBefore(any(LocalDateTime.class)))
                    .willReturn(150L);
            given(adminStatsRepository.countRetainedMembers(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(90L);

            // when
            AdminStatsResponse stats = adminService.getStats();

            // then
            assertThat(stats.getWau()).isEqualTo(100L);
            assertThat(stats.getMau()).isEqualTo(500L);
            assertThat(stats.getWeeklyPostCount()).isEqualTo(50L);
            assertThat(stats.getUploaderRatio()).isEqualTo(20.0); // 40/200 * 100
            assertThat(stats.getRetentionRate()).isEqualTo(60.0); // 90/150 * 100
        }

        @Test
        @DisplayName("통계 조회 - 회원 0명일 때 비율 0")
        void getStats_zeroMembers() {
            // given
            given(adminStatsRepository.countActiveUsersSince(any(LocalDateTime.class)))
                    .willReturn(0L)
                    .willReturn(0L);
            given(adminStatsRepository.countPostsSince(any(LocalDateTime.class)))
                    .willReturn(0L);
            given(adminStatsRepository.countMemberRoleMembers())
                    .willReturn(0L);
            given(adminStatsRepository.countUniquePostAuthorsSince(any(LocalDateTime.class)))
                    .willReturn(0L);
            given(adminStatsRepository.countMembersJoinedBefore(any(LocalDateTime.class)))
                    .willReturn(0L);
            given(adminStatsRepository.countRetainedMembers(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(0L);

            // when
            AdminStatsResponse stats = adminService.getStats();

            // then
            assertThat(stats.getUploaderRatio()).isEqualTo(0.0);
            assertThat(stats.getRetentionRate()).isEqualTo(0.0);
        }
    }
}
