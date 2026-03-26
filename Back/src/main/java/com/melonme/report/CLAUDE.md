# Report 도메인

## 담당 범위
- Report (신고)

## 담당 디렉토리
/src/main/java/.../report/

## 구현 목록
1. 게시글/댓글 신고 접수

## ERD
Report
- id, reporter_id
- target_type(POST/COMMENT), target_id
- reason, status(PENDING/PROCESSED)
- created_at

## 비즈니스 규칙
- 동일 대상 중복 신고 불가
- 본인 게시글/댓글 신고 불가
- 신고 처리는 Admin 도메인에서 담당

## API
POST /api/reports
Auth: 필요
Body: { targetType: string, targetId: number, reason: string }
Return: { }

## 단위테스트 필수 항목
- 중복 신고 예외
- 본인 신고 예외
- 정상 신고 접수
