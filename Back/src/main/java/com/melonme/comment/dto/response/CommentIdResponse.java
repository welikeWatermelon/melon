package com.melonme.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentIdResponse {

    private Long id;

    public static CommentIdResponse of(Long id) {
        return new CommentIdResponse(id);
    }
}
