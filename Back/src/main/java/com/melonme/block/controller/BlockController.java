package com.melonme.block.controller;

import com.melonme.block.dto.response.BlockListResponse;
import com.melonme.block.dto.response.BlockToggleResponse;
import com.melonme.block.service.BlockService;
import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{memberId}")
    public ResponseEntity<ApiResponse<BlockToggleResponse>> toggleBlock(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberId) {
        BlockToggleResponse response = blockService.toggleBlock(userDetails.getMemberId(), memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<BlockListResponse>> getBlockedMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BlockListResponse response = blockService.getBlockedMembers(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
