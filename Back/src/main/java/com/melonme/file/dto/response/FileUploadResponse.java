package com.melonme.file.dto.response;

import com.melonme.file.domain.FileEntity;
import lombok.Builder;

@Builder
public record FileUploadResponse(
        Long fileId,
        String originalName,
        Long fileSize
) {
    public static FileUploadResponse from(FileEntity file) {
        return FileUploadResponse.builder()
                .fileId(file.getId())
                .originalName(file.getOriginalName())
                .fileSize(file.getFileSize())
                .build();
    }
}
