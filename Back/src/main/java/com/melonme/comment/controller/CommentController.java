package com.melonme.comment.controller;

import com.melonme.comment.dto.request.CommentCreateRequest;
import com.melonme.comment.dto.request.CommentUpdateRequest;
import com.melonme.comment.dto.response.CommentIdResponse;
import com.melonme.comment.dto.response.CommentListResponse;
import com.melonme.comment.dto.response.MyCommentResponse;
import com.melonme.comment.service.CommentService;
import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CommentListResponse response = commentService.getComments(postId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentIdResponse>> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentCreateRequest request) {

        String memberRole = userDetails.getRole();
        CommentIdResponse response = commentService.createComment(postId, userDetails.getMemberId(), memberRole, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/posts/{postId}/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentIdResponse>> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentCreateRequest request) {

        String memberRole = userDetails.getRole();
        CommentIdResponse response = commentService.createReply(postId, commentId, userDetails.getMemberId(), memberRole, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentIdResponse>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentUpdateRequest request) {

        CommentIdResponse response = commentService.updateComment(postId, commentId, userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/posts/{postId}/comments/{commentId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        commentService.deleteComment(postId, commentId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/api/members/me/comments")
    public ResponseEntity<ApiResponse<Page<MyCommentResponse>>> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<MyCommentResponse> response = commentService.getMyComments(userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
