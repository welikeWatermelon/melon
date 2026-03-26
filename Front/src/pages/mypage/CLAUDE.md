# 프론트 Mypage 에이전트

## 담당 범위
마이페이지, 알림 (SSE), 차단 목록

## 담당 디렉토리
/src/pages/mypage/
/src/api/memberApi.ts
/src/api/notificationApi.ts
/src/api/blockApi.ts
/src/store/notificationStore.ts

## 선행 조건
- common-agent 완료 후 시작

## 구현 목록

### 마이페이지 (/mypage)
- 내 정보 (닉네임, 치료영역 수정)
- 탭 메뉴:
    - 내가 쓴 글
    - 내가 쓴 댓글
    - 스크랩한 글
    - 차단 목록

### 내가 쓴 글 탭
- 게시글 카드 리스트
- 페이지네이션

### 내가 쓴 댓글 탭
- 댓글 내용 + 원글 제목
- 원글 클릭 시 /board/{postId} 이동

### 스크랩한 글 탭
- 게시글 카드 리스트
- 스크랩 취소 가능

### 차단 목록 탭
- 차단한 회원 목록
- 차단 해제 버튼

### 알림 (GNB 연동)
- SSE 연결 (로그인 시 자동 연결)
- 읽지 않은 알림 수 뱃지
- 알림 드롭다운
    - 알림 목록
    - 개별 읽음 처리
    - 전체 읽음 처리
    - 알림 클릭 시 해당 게시글로 이동
- 로그아웃 시 SSE 연결 종료

## SSE 연결 규칙
- EventSource 사용
- 로그인 성공 시 연결 시작
- 알림 수신 시 notificationStore 업데이트
- 연결 끊기면 3초 후 재연결

## Zustand notificationStore
```typescript
- notifications: Notification[]
- unreadCount: number
- addNotification(notification): void
- markAsRead(id): void
- markAllAsRead(): void
```

## API 연동
- GET/PATCH /api/members/me
- PATCH /api/members/me/delete
- GET /api/notifications/subscribe (SSE)
- GET/PATCH /api/notifications
- GET/POST /api/blocks