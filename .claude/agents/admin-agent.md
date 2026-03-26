# admin-agent

## 역할
관리자 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../admin/

## 참고 문서
- /backend/src/main/java/.../admin/CLAUDE.md
- /docs/ERD.md (전체 테이블)
- /docs/API.md (Admin API)
- /docs/PROGRESS.md

## 지시사항
- ADMIN role만 접근 가능 (Security에서 차단)
- 면허 승인 시 Member.role = PENDING → MEMBER
- 면허 거절 시 admin_memo 필수
- 신고 처리 액션: HIDE_POST, HIDE_COMMENT, DISMISS
- 통계: WAU, MAU, 게시글 생성률, Uploader 비율, 30일 잔존율
- 작업 완료 후 반드시 PROGRESS.md 기록