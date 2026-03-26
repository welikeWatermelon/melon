package com.melonme.admin.controller;

import com.melonme.admin.dto.request.LicenseReviewRequest;
import com.melonme.admin.dto.request.MemberActionRequest;
import com.melonme.admin.dto.request.ReportProcessRequest;
import com.melonme.admin.dto.response.AdminLicenseResponse;
import com.melonme.admin.dto.response.AdminMemberResponse;
import com.melonme.admin.dto.response.AdminReportResponse;
import com.melonme.admin.dto.response.AdminStatsResponse;
import com.melonme.admin.service.AdminService;
import com.melonme.global.response.ApiResponse;
import com.melonme.post.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.melonme.global.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ======================== 면허 인증 ========================

    @GetMapping("/licenses")
    public ResponseEntity<ApiResponse<PageResponse<AdminLicenseResponse>>> getLicenses(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminLicenseResponse> page = adminService.getLicenses(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PatchMapping("/licenses/{id}")
    public ResponseEntity<ApiResponse<Void>> reviewLicense(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid LicenseReviewRequest request) {
        Long adminId = userDetails.getMemberId();
        adminService.reviewLicense(id, adminId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ======================== 신고 ========================

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<PageResponse<AdminReportResponse>>> getReports(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminReportResponse> page = adminService.getReports(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PatchMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<Void>> processReport(
            @PathVariable Long id,
            @RequestBody @Valid ReportProcessRequest request) {
        adminService.processReport(id, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ======================== 회원 ========================

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<PageResponse<AdminMemberResponse>>> getMembers(
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminMemberResponse> page = adminService.getMembers(role, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PatchMapping("/members/{id}")
    public ResponseEntity<ApiResponse<Void>> actionMember(
            @PathVariable Long id,
            @RequestBody @Valid MemberActionRequest request) {
        adminService.actionMember(id, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ======================== 통계 ========================

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        AdminStatsResponse stats = adminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
