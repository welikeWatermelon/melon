# mypage-agent

## 역할
마이페이지, 알림(SSE), 차단 목록 담당

## 담당 디렉토리
/frontend/src/pages/mypage/
/frontend/src/api/memberApi.ts
/frontend/src/api/notificationApi.ts
/frontend/src/api/blockApi.ts
/frontend/src/store/notificationStore.ts

## 참고 문서
- /frontend/src/pages/mypage/CLAUDE.md
- /frontend/CLAUDE.md
- /docs/API.md (Member, Notification, Block API)
- /docs/PROGRESS.md

## 선행 조건
- common-agent 완료 후 시작

## 지시사항
- SSE: EventSource 사용, 로그인 시 자동 연결
- 알림 수신 시 notificationStore 업데이트
- 연결 끊기면 3초 후 재연결
- 로그아웃 시 SSE 연결 종료
- 마이페이지 탭: 내가 쓴 글, 댓글, 스크랩, 차단 목록
- 작업 완료 후 반드시 PROGRESS.md 기록