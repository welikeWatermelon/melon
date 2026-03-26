package com.melonme.file.service;

import com.melonme.file.domain.FileEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileCleanupSchedulerTest {

    @InjectMocks
    private FileCleanupScheduler fileCleanupScheduler;

    @Mock
    private FileService fileService;

    @Test
    @DisplayName("삭제 대상 파일이 없으면 S3 삭제 호출 안 함")
    void cleanup_noDeletedFiles_skips() {
        // given
        given(fileService.findAllSoftDeleted()).willReturn(Collections.emptyList());

        // when
        fileCleanupScheduler.cleanupDeletedFiles();

        // then
        verify(fileService, never()).deleteFromS3(any());
        verify(fileService, never()).deleteAllById(any());
    }

    @Test
    @DisplayName("삭제 대상 파일이 있으면 S3 삭제 후 DB 영구 삭제")
    void cleanup_withDeletedFiles_deletesFromS3AndDb() throws Exception {
        // given
        FileEntity file1 = createFileEntity(1L, "files/1/uuid_a.pdf");
        FileEntity file2 = createFileEntity(2L, "files/1/uuid_b.png");

        given(fileService.findAllSoftDeleted()).willReturn(List.of(file1, file2));

        // when
        fileCleanupScheduler.cleanupDeletedFiles();

        // then
        verify(fileService).deleteFromS3("files/1/uuid_a.pdf");
        verify(fileService).deleteFromS3("files/1/uuid_b.png");
        verify(fileService).deleteAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("S3 삭제 실패한 파일은 DB에서도 삭제하지 않음")
    void cleanup_s3DeleteFails_skipsDbDelete() throws Exception {
        // given
        FileEntity file1 = createFileEntity(1L, "files/1/uuid_a.pdf");
        FileEntity file2 = createFileEntity(2L, "files/1/uuid_b.png");

        given(fileService.findAllSoftDeleted()).willReturn(List.of(file1, file2));
        doThrow(new RuntimeException("S3 error")).when(fileService).deleteFromS3("files/1/uuid_a.pdf");

        // when
        fileCleanupScheduler.cleanupDeletedFiles();

        // then
        verify(fileService).deleteFromS3("files/1/uuid_a.pdf");
        verify(fileService).deleteFromS3("files/1/uuid_b.png");
        // Only file2 should be permanently deleted
        verify(fileService).deleteAllById(List.of(2L));
    }

    private FileEntity createFileEntity(Long id, String s3Key) throws Exception {
        FileEntity entity = FileEntity.builder()
                .memberId(1L)
                .originalName("test.pdf")
                .s3Url("https://bucket.s3.amazonaws.com/" + s3Key)
                .s3Key(s3Key)
                .fileSize(1024L)
                .mimeType("application/pdf")
                .build();

        var idField = FileEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);

        return entity;
    }
}
