package com.melonme.like.controller;

import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import com.melonme.like.dto.request.LikeToggleRequest;
import com.melonme.like.dto.response.LikeToggleResponse;
import com.melonme.like.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<ApiResponse<LikeToggleResponse>> toggle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LikeToggleRequest request
    ) {
        LikeToggleResponse response = likeService.toggle(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
