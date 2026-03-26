package com.melonme.post.controller;

import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import com.melonme.post.dto.request.PostCreateRequest;
import com.melonme.post.dto.request.PostUpdateRequest;
import com.melonme.post.dto.response.*;
import com.melonme.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 API 컨트롤러
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListResponse>>> getPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String therapyArea,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<PostListResponse> response = postService.getPosts(userDetails.getMemberId(), therapyArea, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        PostDetailResponse response = postService.getPost(postId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostCreateRequest request) {

        String memberRole = userDetails.getRole();
        PostCreateResponse response = postService.createPost(userDetails.getMemberId(), memberRole, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 게시글 수정
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostCreateResponse>> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostUpdateRequest request) {

        PostCreateResponse response = postService.updatePost(postId, userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 삭제 (soft delete)
     */
    @PatchMapping("/{postId}/delete")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        postService.deletePost(postId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 내가 쓴 게시글 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<PostListResponse>>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<PostListResponse> response = postService.getMyPosts(userDetails.getMemberId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스크랩한 게시글 목록
     */
    @GetMapping("/scrapped")
    public ResponseEntity<ApiResponse<PageResponse<PostListResponse>>> getScrappedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<PostListResponse> response = postService.getScrappedPosts(userDetails.getMemberId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 스크랩 토글
     */
    @PostMapping("/{postId}/scraps")
    public ResponseEntity<ApiResponse<ScrapResponse>> toggleScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ScrapResponse response = postService.toggleScrap(postId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
