package com.melonme.member.controller;

import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import com.melonme.member.domain.Provider;
import com.melonme.member.dto.request.TokenRefreshRequest;
import com.melonme.member.dto.response.LoginResponse;
import com.melonme.member.dto.response.TokenRefreshResponse;
import com.melonme.member.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@RequestParam String code) {
        LoginResponse response = authService.socialLogin(Provider.KAKAO, code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestParam String code) {
        LoginResponse response = authService.socialLogin(Provider.GOOGLE, code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
