package com.melonme.comment.dto.response;

import com.melonme.comment.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyCommentResponse {

    private Long id;
    private String content;
    private Long postId;
    private String postTitle;
    private LocalDateTime createdAt;

    public static MyCommentResponse of(Comment comment, String postTitle) {
        return MyCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPostId())
                .postTitle(postTitle)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
