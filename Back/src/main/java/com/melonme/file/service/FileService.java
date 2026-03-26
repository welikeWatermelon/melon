package com.melonme.file.service;

import com.melonme.file.domain.FileEntity;
import com.melonme.file.dto.response.FileUploadResponse;
import com.melonme.file.repository.FileRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "hwp", "jpg", "jpeg", "png", "gif"
    );
    private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository,
                       @Autowired(required = false) S3Client s3Client,
                       @Autowired(required = false) S3Presigner s3Presigner) {
        this.fileRepository = fileRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Value("${S3_BUCKET:melonme-bucket}")
    private String bucket;

    @Transactional
    public FileUploadResponse upload(MultipartFile file, Long memberId) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        String s3Key = generateS3Key(memberId, originalName);

        uploadToS3(file, s3Key, file.getContentType());

        String s3Url = String.format("https://%s.s3.amazonaws.com/%s", bucket, s3Key);

        FileEntity fileEntity = FileEntity.builder()
                .memberId(memberId)
                .originalName(originalName)
                .s3Url(s3Url)
                .s3Key(s3Key)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();

        FileEntity saved = fileRepository.save(fileEntity);
        return FileUploadResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public String generatePresignedUrl(Long fileId) {
        FileEntity file = findFileOrThrow(fileId);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(file.getS3Key())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_TTL)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    @Transactional
    public void softDelete(Long fileId, Long memberId) {
        FileEntity file = findFileOrThrow(fileId);

        if (!file.getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        file.softDelete();
    }

    public void deleteFromS3(String s3Key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();
        s3Client.deleteObject(deleteRequest);
    }

    public List<FileEntity> findAllSoftDeleted() {
        return fileRepository.findAllSoftDeleted();
    }

    public void deleteAllById(List<Long> ids) {
        fileRepository.deleteAllById(ids);
    }

    // -- internal methods --

    void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    String generateS3Key(Long memberId, String originalName) {
        return String.format("files/%d/%s_%s", memberId, UUID.randomUUID(), originalName);
    }

    private void uploadToS3(MultipartFile file, String s3Key, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private FileEntity findFileOrThrow(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
    }
}
