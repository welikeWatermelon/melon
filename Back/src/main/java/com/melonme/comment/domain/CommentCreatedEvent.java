package com.melonme.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreatedEvent {

    private final Long commentId;
    private final Long postId;
    private final Long memberId;
    private final Long parentCommentId;
    private final Long postAuthorId;
    /** 대댓글인 경우 부모 댓글 작성자 ID, 일반 댓글이면 null */
    private final Long parentCommentAuthorId;

    public boolean isReply() {
        return parentCommentId != null;
    }
}
