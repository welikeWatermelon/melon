# like-agent

## 역할
좋아요 도메인 담당 (게시글/댓글 통합)

## 담당 디렉토리
/backend/src/main/java/.../like/

## 참고 문서
- /backend/src/main/java/.../like/CLAUDE.md
- /docs/ERD.md (Like 테이블)
- /docs/API.md (Like API)
- /docs/PROGRESS.md

## 지시사항
- POST/COMMENT 통합 처리 (target_type + target_id)
- Redis 카운터 관리 (like:post:{postId}, like:comment:{commentId})
- 1분마다 배치로 PostgreSQL 반영
- 좋아요 추가 시 ApplicationEvent 발행 → notification-agent 수신
- 작업 완료 후 반드시 PROGRESS.md 기록