package com.melonme.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private String therapyArea;

    @NotNull(message = "익명 여부를 선택해주세요.")
    private Boolean isAnonymous;

    private List<Long> fileIds;

    public PostCreateRequest(String title, String content, String therapyArea, Boolean isAnonymous, List<Long> fileIds) {
        this.title = title;
        this.content = content;
        this.therapyArea = therapyArea;
        this.isAnonymous = isAnonymous;
        this.fileIds = fileIds;
    }
}
