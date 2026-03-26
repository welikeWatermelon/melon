package com.melonme.file.controller;

import com.melonme.file.dto.response.FileUploadResponse;
import com.melonme.file.service.FileService;
import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        FileUploadResponse response = fileService.upload(file, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Void> download(@PathVariable Long fileId) {
        String presignedUrl = fileService.generatePresignedUrl(fileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }

    @PatchMapping("/{fileId}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long fileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        fileService.softDelete(fileId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
