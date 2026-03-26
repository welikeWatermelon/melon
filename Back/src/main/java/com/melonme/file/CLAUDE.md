# File 도메인

## 담당 범위
- File (S3 업로드/다운로드)

## 담당 디렉토리
/src/main/java/.../file/
/src/main/java/.../global/config/S3Config.java

## 구현 목록
1. 파일 S3 업로드
2. 파일 다운로드 (S3 presigned URL)
3. 파일 soft delete
4. 배치: 삭제된 파일 S3에서 실제 제거

## ERD
File
- id, member_id, post_id(nullable)
- original_name, s3_url
- file_size, mime_type
- created_at, deleted_at

## 비즈니스 규칙
- 허용 파일: pdf, hwp, jpg, jpeg, png, gif
- 최대 파일 크기: 10MB
- S3 키 구조: files/{memberId}/{UUID}_{originalName}
- 삭제는 soft delete (deleted_at 업데이트)
- 실제 S3 삭제는 배치로 처리 (매일 새벽 3시)
- presigned URL TTL: 10분

## S3 설정
- 버킷: 환경변수로 주입
- ACL: private (presigned URL로만 접근)
- 업로드 후 s3_url DB 저장

## API
POST /api/files/upload
Return: { fileId, s3Url, originalName }

GET  /api/files/{fileId}/download
Return: presigned URL redirect

## 단위테스트 필수 항목
- 허용되지 않는 파일 형식 예외
- 파일 크기 초과 예외
- S3 업로드 성공 후 DB 저장
- presigned URL 생성
- 타인 파일 다운로드 권한 확인