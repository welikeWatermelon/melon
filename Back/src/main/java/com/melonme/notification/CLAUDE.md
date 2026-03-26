# Notification 도메인

## 담당 범위
- Notification (알림)
- SSE (Server-Sent Events) 연결 관리

## 담당 디렉토리
/src/main/java/.../notification/

## 구현 목록
1. SSE 연결 (subscribe)
2. 알림 발송 (내부 이벤트 기반)
3. 알림 목록 조회
4. 개별 읽음 처리
5. 전체 읽음 처리

## ERD
Notification
- id, receiver_id, sender_id
- type(COMMENT_ON_POST/REPLY_ON_COMMENT/LIKE_ON_POST/LIKE_ON_COMMENT)
- target_type(POST/COMMENT), target_id
- is_read(DEFAULT FALSE)
- created_at

## SSE 구현 규칙
- SseEmitter 사용
- 연결 timeout: 30분
- Map<Long, SseEmitter> 로 memberId별 관리
- 연결 즉시 더미 이벤트 전송 (503 방지)
- 연결 끊기면 Map에서 제거

## 알림 발송 시점
- 내 게시글에 댓글    → COMMENT_ON_POST
- 내 댓글에 대댓글    → REPLY_ON_COMMENT
- 내 게시글에 좋아요  → LIKE_ON_POST
- 내 댓글에 좋아요    → LIKE_ON_COMMENT

## 알림 발송 방식
- Spring ApplicationEvent 사용
- 각 도메인(Comment, Like)에서 이벤트 발행
- Notification 도메인에서 이벤트 수신 후 처리
- SSE 연결 중이면 즉시 전송
- 연결 없어도 DB에는 항상 저장

## 비즈니스 규칙
- 본인 행위는 알림 발송 안 함
  (내 게시글에 내가 댓글 달아도 알림 없음)
- 차단한 회원의 알림은 발송 안 함
- 알림 목록은 최신순 정렬

## API
GET   /api/notifications/subscribe   SSE 연결
GET   /api/notifications             알림 목록 (page, size)
PATCH /api/notifications/read-all    전체 읽음
PATCH /api/notifications/{id}/read   개별 읽음

## 단위테스트 필수 항목
- 본인 행위 알림 미발송
- 차단 회원 알림 미발송
- SSE 연결 없을 때 DB 저장 확인
- 읽음 처리 (개별/전체)
- 알림 목록 페이지네이션