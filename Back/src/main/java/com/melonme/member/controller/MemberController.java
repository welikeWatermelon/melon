package com.melonme.member.controller;

import com.melonme.global.response.ApiResponse;
import com.melonme.global.security.CustomUserDetails;
import com.melonme.member.dto.request.MemberUpdateRequest;
import com.melonme.member.dto.response.LicenseResponse;
import com.melonme.member.dto.response.MemberInfoResponse;
import com.melonme.member.dto.response.MemberUpdateResponse;
import com.melonme.member.service.LicenseService;
import com.melonme.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final LicenseService licenseService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberInfoResponse response = memberService.getMyInfo(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberUpdateResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MemberUpdateRequest request) {
        MemberUpdateResponse response = memberService.updateMyInfo(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.deleteMe(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/me/license")
    public ResponseEntity<ApiResponse<LicenseResponse>> applyLicense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("licenseImg") MultipartFile licenseImg) {
        LicenseResponse response = licenseService.applyLicense(userDetails.getMemberId(), licenseImg);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/license")
    public ResponseEntity<ApiResponse<LicenseResponse>> getMyLicense(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        LicenseResponse response = licenseService.getMyLicense(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
