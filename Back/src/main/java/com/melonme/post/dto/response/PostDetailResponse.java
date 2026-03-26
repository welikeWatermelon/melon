package com.melonme.post.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String author;
    private boolean isAnonymous;
    private String therapyArea;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private boolean isLiked;
    private boolean isScrapped;
    private boolean isMyPost;
    private List<FileInfo> files;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class FileInfo {
        private Long id;
        private String originalName;
        private long fileSize;
    }
}
