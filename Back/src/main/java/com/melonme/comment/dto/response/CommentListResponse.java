package com.melonme.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentListResponse {

    private List<CommentResponse> comments;

    public static CommentListResponse of(List<CommentResponse> comments) {
        return new CommentListResponse(comments);
    }
}
