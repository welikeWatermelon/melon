package com.melonme.file.service;

import com.melonme.file.domain.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final FileService fileService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedFiles() {
        List<FileEntity> deletedFiles = fileService.findAllSoftDeleted();

        if (deletedFiles.isEmpty()) {
            log.info("삭제 대상 파일이 없습니다.");
            return;
        }

        log.info("삭제 대상 파일 {}건 처리 시작", deletedFiles.size());

        List<Long> deletedIds = new java.util.ArrayList<>();

        for (FileEntity file : deletedFiles) {
            try {
                fileService.deleteFromS3(file.getS3Key());
                deletedIds.add(file.getId());
                log.debug("S3 파일 삭제 완료: {}", file.getS3Key());
            } catch (Exception e) {
                log.error("S3 파일 삭제 실패: {} - {}", file.getS3Key(), e.getMessage());
            }
        }

        if (!deletedIds.isEmpty()) {
            fileService.deleteAllById(deletedIds);
            log.info("파일 DB 레코드 {}건 영구 삭제 완료", deletedIds.size());
        }
    }
}
