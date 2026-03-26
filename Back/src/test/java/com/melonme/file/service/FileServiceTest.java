package com.melonme.file.service;

import com.melonme.file.domain.FileEntity;
import com.melonme.file.dto.response.FileUploadResponse;
import com.melonme.file.repository.FileRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private FileRepository fileRepository;

    private static final Long MEMBER_ID = 1L;
    private static final Long FILE_ID = 10L;

    // ── 업로드 관련 테스트 ──

    @Test
    @DisplayName("허용되지 않는 파일 형식이면 FILE_TYPE_NOT_ALLOWED 예외")
    void upload_invalidExtension_throwsException() {
        // given
        MultipartFile file = mockMultipartFile("test.exe", "application/octet-stream", 1024);

        // when & then
        assertThatThrownBy(() -> fileService.upload(file, MEMBER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED));
    }

    @Test
    @DisplayName("파일 크기가 10MB 초과하면 FILE_SIZE_EXCEEDED 예외")
    void upload_fileTooLarge_throwsException() {
        // given
        long overSize = 11 * 1024 * 1024; // 11MB
        MultipartFile file = mockMultipartFile("test.pdf", "application/pdf", overSize);

        // when & then
        assertThatThrownBy(() -> fileService.upload(file, MEMBER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FILE_SIZE_EXCEEDED));
    }

    @Test
    @DisplayName("S3 업로드 성공 후 DB에 FileEntity 저장")
    void upload_success_savesToDb() throws Exception {
        // given
        MultipartFile file = mockMultipartFile("document.pdf", "application/pdf", 5000);
        given(file.getBytes()).willReturn(new byte[5000]);

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        FileEntity savedEntity = FileEntity.builder()
                .memberId(MEMBER_ID)
                .originalName("document.pdf")
                .s3Url("https://melonme-bucket.s3.amazonaws.com/files/1/uuid_document.pdf")
                .s3Key("files/1/uuid_document.pdf")
                .fileSize(5000L)
                .mimeType("application/pdf")
                .build();

        // Use reflection to set id for testing
        var idField = FileEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(savedEntity, FILE_ID);

        given(fileRepository.save(any(FileEntity.class))).willReturn(savedEntity);

        // when
        FileUploadResponse response = fileService.upload(file, MEMBER_ID);

        // then
        assertThat(response.fileId()).isEqualTo(FILE_ID);
        assertThat(response.originalName()).isEqualTo("document.pdf");
        assertThat(response.fileSize()).isEqualTo(5000L);

        ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
        verify(fileRepository).save(captor.capture());
        FileEntity captured = captor.getValue();
        assertThat(captured.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(captured.getOriginalName()).isEqualTo("document.pdf");
        assertThat(captured.getMimeType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("허용 확장자 pdf, hwp, jpg, jpeg, png, gif 모두 통과")
    void upload_allowedExtensions_noException() {
        // given
        String[] extensions = {"pdf", "hwp", "jpg", "jpeg", "png", "gif"};

        for (String ext : extensions) {
            MultipartFile file = mockMultipartFile("test." + ext, "application/octet-stream", 1024);
            // validateFile should not throw
            fileService.validateFile(file);
        }
    }

    // ── presigned URL 테스트 ──

    @Test
    @DisplayName("presigned URL 생성 성공")
    void generatePresignedUrl_success() throws Exception {
        // given
        FileEntity file = FileEntity.builder()
                .memberId(MEMBER_ID)
                .originalName("test.pdf")
                .s3Url("https://melonme-bucket.s3.amazonaws.com/files/1/uuid_test.pdf")
                .s3Key("files/1/uuid_test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .build();

        var idField = FileEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(file, FILE_ID);

        given(fileRepository.findById(FILE_ID)).willReturn(Optional.of(file));

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        given(presignedRequest.url()).willReturn(new URL("https://presigned-url.example.com"));
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        String url = fileService.generatePresignedUrl(FILE_ID);

        // then
        assertThat(url).isEqualTo("https://presigned-url.example.com");
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 FILE_NOT_FOUND 예외")
    void generatePresignedUrl_fileNotFound_throwsException() {
        // given
        given(fileRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.generatePresignedUrl(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FILE_NOT_FOUND));
    }

    // ── soft delete 테스트 ──

    @Test
    @DisplayName("타인의 파일 삭제 시 AUTH_FORBIDDEN 예외")
    void softDelete_otherMemberFile_throwsException() throws Exception {
        // given
        Long otherMemberId = 999L;
        FileEntity file = FileEntity.builder()
                .memberId(otherMemberId)
                .originalName("test.pdf")
                .s3Url("url")
                .s3Key("key")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .build();

        var idField = FileEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(file, FILE_ID);

        given(fileRepository.findById(FILE_ID)).willReturn(Optional.of(file));

        // when & then
        assertThatThrownBy(() -> fileService.softDelete(FILE_ID, MEMBER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    @Test
    @DisplayName("본인 파일 soft delete 성공")
    void softDelete_ownFile_success() throws Exception {
        // given
        FileEntity file = FileEntity.builder()
                .memberId(MEMBER_ID)
                .originalName("test.pdf")
                .s3Url("url")
                .s3Key("key")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .build();

        var idField = FileEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(file, FILE_ID);

        given(fileRepository.findById(FILE_ID)).willReturn(Optional.of(file));

        // when
        fileService.softDelete(FILE_ID, MEMBER_ID);

        // then
        assertThat(file.isDeleted()).isTrue();
    }

    // ── helper ──

    private MultipartFile mockMultipartFile(String filename, String contentType, long size) {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.getOriginalFilename()).thenReturn(filename);
        lenient().when(file.getContentType()).thenReturn(contentType);
        lenient().when(file.getSize()).thenReturn(size);
        lenient().when(file.isEmpty()).thenReturn(false);
        return file;
    }
}
