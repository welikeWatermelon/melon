# notification-agent

## 역할
알림/SSE 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../notification/

## 참고 문서
- /backend/src/main/java/.../notification/CLAUDE.md
- /docs/ERD.md (Notification 테이블)
- /docs/API.md (Notification API)
- /docs/PROGRESS.md

## 지시사항
- SseEmitter 사용, 연결 timeout 30분
- Map<Long, SseEmitter>로 memberId별 관리
- 연결 즉시 더미 이벤트 전송 (503 방지)
- ApplicationEvent 수신: CommentCreatedEvent, LikeCreatedEvent
- 본인 행위 알림 미발송
- 차단 회원 알림 미발송
- 작업 완료 후 반드시 PROGRESS.md 기록