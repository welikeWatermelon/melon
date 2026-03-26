package com.melonme.report.service;

import com.melonme.comment.domain.Comment;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.post.domain.Post;
import com.melonme.post.repository.PostRepository;
import com.melonme.report.domain.Report;
import com.melonme.report.domain.TargetType;
import com.melonme.report.dto.request.ReportCreateRequest;
import com.melonme.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Nested
    @DisplayName("게시글 신고")
    class ReportPost {

        @Test
        @DisplayName("정상 신고 접수 - 게시글")
        void createReport_post_success() {
            // given
            Long reporterId = 1L;
            Long targetId = 10L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.POST, targetId, "부적절한 내용");

            Post post = Post.builder()
                    .memberId(2L)
                    .title("test")
                    .content("content")
                    .therapyArea("언어")
                    .isAnonymous(false)
                    .build();

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.POST, targetId))
                    .willReturn(false);
            given(postRepository.findById(targetId)).willReturn(Optional.of(post));
            given(reportRepository.save(any(Report.class))).willReturn(mock(Report.class));

            // when
            reportService.createReport(reporterId, request);

            // then
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("중복 신고 예외 - 게시글")
        void createReport_post_duplicate() {
            // given
            Long reporterId = 1L;
            Long targetId = 10L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.POST, targetId, "부적절한 내용");

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.POST, targetId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assert customEx.getErrorCode() == ErrorCode.REPORT_DUPLICATE;
                    });

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("본인 게시글 신고 예외")
        void createReport_post_self() {
            // given
            Long reporterId = 1L;
            Long targetId = 10L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.POST, targetId, "부적절한 내용");

            Post post = Post.builder()
                    .memberId(1L) // same as reporterId
                    .title("test")
                    .content("content")
                    .therapyArea("언어")
                    .isAnonymous(false)
                    .build();

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.POST, targetId))
                    .willReturn(false);
            given(postRepository.findById(targetId)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assert customEx.getErrorCode() == ErrorCode.REPORT_SELF;
                    });

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 신고 예외")
        void createReport_post_notFound() {
            // given
            Long reporterId = 1L;
            Long targetId = 999L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.POST, targetId, "부적절한 내용");

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.POST, targetId))
                    .willReturn(false);
            given(postRepository.findById(targetId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assert customEx.getErrorCode() == ErrorCode.REPORT_TARGET_NOT_FOUND;
                    });

            verify(reportRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("댓글 신고")
    class ReportComment {

        @Test
        @DisplayName("정상 신고 접수 - 댓글")
        void createReport_comment_success() {
            // given
            Long reporterId = 1L;
            Long targetId = 20L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.COMMENT, targetId, "욕설");

            Comment comment = Comment.builder()
                    .postId(10L)
                    .memberId(2L)
                    .content("test comment")
                    .isAnonymous(false)
                    .build();

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.COMMENT, targetId))
                    .willReturn(false);
            given(commentRepository.findById(targetId)).willReturn(Optional.of(comment));
            given(reportRepository.save(any(Report.class))).willReturn(mock(Report.class));

            // when
            reportService.createReport(reporterId, request);

            // then
            verify(reportRepository).save(any(Report.class));
        }

        @Test
        @DisplayName("본인 댓글 신고 예외")
        void createReport_comment_self() {
            // given
            Long reporterId = 1L;
            Long targetId = 20L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.COMMENT, targetId, "욕설");

            Comment comment = Comment.builder()
                    .postId(10L)
                    .memberId(1L) // same as reporterId
                    .content("test comment")
                    .isAnonymous(false)
                    .build();

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.COMMENT, targetId))
                    .willReturn(false);
            given(commentRepository.findById(targetId)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assert customEx.getErrorCode() == ErrorCode.REPORT_SELF;
                    });

            verify(reportRepository, never()).save(any());
        }

        @Test
        @DisplayName("중복 신고 예외 - 댓글")
        void createReport_comment_duplicate() {
            // given
            Long reporterId = 1L;
            Long targetId = 20L;
            ReportCreateRequest request = new ReportCreateRequest(TargetType.COMMENT, targetId, "욕설");

            given(reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, TargetType.COMMENT, targetId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assert customEx.getErrorCode() == ErrorCode.REPORT_DUPLICATE;
                    });

            verify(reportRepository, never()).save(any());
        }
    }
}
