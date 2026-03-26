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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void createReport(Long reporterId, ReportCreateRequest request) {
        // 중복 신고 검증
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporterId, request.getTargetType(), request.getTargetId())) {
            throw new CustomException(ErrorCode.REPORT_DUPLICATE);
        }

        // 본인 신고 검증
        Long targetOwnerId = findTargetOwnerId(request.getTargetType(), request.getTargetId());
        if (reporterId.equals(targetOwnerId)) {
            throw new CustomException(ErrorCode.REPORT_SELF);
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .build();

        reportRepository.save(report);
    }

    private Long findTargetOwnerId(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> postRepository.findById(targetId)
                    .map(Post::getMemberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND));
            case COMMENT -> commentRepository.findById(targetId)
                    .map(Comment::getMemberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_TARGET_NOT_FOUND));
        };
    }
}
