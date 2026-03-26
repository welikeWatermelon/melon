package com.melonme.comment.dto.response;

import com.melonme.comment.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private String author;
    private String content;
    private boolean isAnonymous;
    private int likeCount;
    private boolean isMyComment;
    private LocalDateTime createdAt;
    private List<ReplyResponse> replies;

    public static CommentResponse of(Comment comment, String author, boolean isMyComment, List<ReplyResponse> replies) {
        return CommentResponse.builder()
                .id(comment.getId())
                .author(author)
                .content(comment.getContent())
                .isAnonymous(comment.isAnonymous())
                .likeCount(comment.getLikeCount())
                .isMyComment(isMyComment)
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }

    public static CommentResponse deleted(Comment comment, List<ReplyResponse> replies) {
        return CommentResponse.builder()
                .id(comment.getId())
                .author("")
                .content("삭제된 댓글입니다")
                .isAnonymous(false)
                .likeCount(0)
                .isMyComment(false)
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }

    public static CommentResponse blocked(Comment comment, boolean isMyComment, List<ReplyResponse> replies) {
        return CommentResponse.builder()
                .id(comment.getId())
                .author("")
                .content("차단한 사용자의 댓글입니다")
                .isAnonymous(false)
                .likeCount(0)
                .isMyComment(isMyComment)
                .createdAt(comment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
