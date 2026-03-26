# comment-agent

## 역할
댓글/대댓글 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../comment/

## 참고 문서
- /backend/src/main/java/.../comment/CLAUDE.md
- /docs/ERD.md (Comment 테이블)
- /docs/API.md (Comment API)
- /docs/PROGRESS.md

## 지시사항
- 대댓글 1depth만 허용 (대댓글의 대댓글 불가)
- 삭제된 댓글: 대댓글 있으면 "삭제된 댓글입니다", 없으면 숨김
- 차단 회원 댓글: "차단한 사용자의 댓글입니다" 처리
- 댓글 작성 시 ApplicationEvent 발행 → notification-agent 수신
- 작업 완료 후 반드시 PROGRESS.md 기록