# file-agent

## 역할
파일 업로드/다운로드 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../file/
/backend/src/main/java/.../global/config/S3Config.java

## 참고 문서
- /backend/src/main/java/.../file/CLAUDE.md
- /docs/ERD.md (File 테이블)
- /docs/API.md (File API)
- /docs/PROGRESS.md

## 지시사항
- 허용 형식: pdf, hwp, jpg, jpeg, png, gif
- 최대 크기: 10MB
- S3 키 구조: files/{memberId}/{UUID}_{originalName}
- 다운로드: presigned URL (TTL 10분)
- 파일 삭제: soft delete, 매일 새벽 3시 배치로 S3 실제 삭제
- 작업 완료 후 반드시 PROGRESS.md 기록