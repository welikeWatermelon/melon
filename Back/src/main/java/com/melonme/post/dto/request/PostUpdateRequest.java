package com.melonme.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    private String title;
    private String content;
    private String therapyArea;
    private List<Long> fileIds;

    public PostUpdateRequest(String title, String content, String therapyArea, List<Long> fileIds) {
        this.title = title;
        this.content = content;
        this.therapyArea = therapyArea;
        this.fileIds = fileIds;
    }
}
