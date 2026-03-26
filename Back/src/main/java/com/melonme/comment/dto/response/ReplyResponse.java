package com.melonme.comment.dto.response;

import com.melonme.comment.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReplyResponse {

    private Long id;
    private String author;
    private String content;
    private boolean isAnonymous;
    private int likeCount;
    private boolean isMyComment;
    private LocalDateTime createdAt;

    public static ReplyResponse of(Comment reply, String author, boolean isMyComment) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .author(author)
                .content(reply.getContent())
                .isAnonymous(reply.isAnonymous())
                .likeCount(reply.getLikeCount())
                .isMyComment(isMyComment)
                .createdAt(reply.getCreatedAt())
                .build();
    }

    public static ReplyResponse deleted(Comment reply) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .author("")
                .content("삭제된 댓글입니다")
                .isAnonymous(false)
                .likeCount(0)
                .isMyComment(false)
                .createdAt(reply.getCreatedAt())
                .build();
    }

    public static ReplyResponse blocked(Comment reply, boolean isMyComment) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .author("")
                .content("차단한 사용자의 댓글입니다")
                .isAnonymous(false)
                .likeCount(0)
                .isMyComment(isMyComment)
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
