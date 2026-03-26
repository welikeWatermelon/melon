package com.melonme.member.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.License;
import com.melonme.member.domain.LicenseStatus;
import com.melonme.member.domain.Member;
import com.melonme.member.dto.response.LicenseResponse;
import com.melonme.member.repository.LicenseRepository;
import com.melonme.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;

    @Autowired
    public LicenseService(LicenseRepository licenseRepository,
                          MemberRepository memberRepository,
                          @Autowired(required = false) S3Client s3Client) {
        this.licenseRepository = licenseRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
    }

    @Value("${cloud.aws.s3.bucket:melonme-bucket}")
    private String bucket;

    @Transactional
    public LicenseResponse applyLicense(Long memberId, MultipartFile licenseImg) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 PENDING 상태인 신청이 있으면 중복 신청 불가
        if (licenseRepository.existsByMemberIdAndStatus(memberId, LicenseStatus.PENDING)) {
            throw new CustomException(ErrorCode.LICENSE_ALREADY_PENDING);
        }

        String s3Url = uploadToS3(licenseImg, memberId);

        License license = License.builder()
                .member(member)
                .licenseImgUrl(s3Url)
                .build();

        licenseRepository.save(license);
        return LicenseResponse.from(license);
    }

    @Transactional(readOnly = true)
    public LicenseResponse getMyLicense(Long memberId) {
        License license = licenseRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.LICENSE_NOT_FOUND));
        return LicenseResponse.from(license);
    }

    private String uploadToS3(MultipartFile file, Long memberId) {
        String key = "license/" + memberId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            return "https://" + bucket + ".s3.amazonaws.com/" + key;
        } catch (IOException e) {
            log.error("S3 upload failed for member {}", memberId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
