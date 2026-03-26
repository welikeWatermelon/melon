package com.melonme.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "license")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "license_img_url", nullable = false)
    private String licenseImgUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseStatus status;

    @Column(name = "admin_memo")
    private String adminMemo;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public License(Member member, String licenseImgUrl) {
        this.member = member;
        this.licenseImgUrl = licenseImgUrl;
        this.status = LicenseStatus.PENDING;
    }

    public void approve(Long adminId) {
        this.status = LicenseStatus.APPROVED;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(Long adminId, String adminMemo) {
        this.status = LicenseStatus.REJECTED;
        this.reviewedBy = adminId;
        this.adminMemo = adminMemo;
        this.reviewedAt = LocalDateTime.now();
    }
}
