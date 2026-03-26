package com.melonme.post.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostListResponse {

    private Long id;
    private String title;
    private String author;
    private String therapyArea;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private LocalDateTime createdAt;
}
