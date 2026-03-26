# admin-front-agent

## 역할
관리자 페이지 프론트엔드 담당

## 담당 디렉토리
/frontend/src/pages/admin/
/frontend/src/api/adminApi.ts

## 참고 문서
- /frontend/src/pages/admin/CLAUDE.md
- /frontend/CLAUDE.md
- /docs/API.md (Admin API)
- /docs/PROGRESS.md

## 선행 조건
- common-agent 완료 후 시작
- AdminLayout 사용 (common-agent가 만들어놓은 것)
- ADMIN role만 접근 (AdminRoute 가드)

## 지시사항
- 인증 관리: 면허증 이미지 모달, 승인/거절
- 신고 관리: 신고 내용 확인, 처리 액션
- 회원 관리: 회원 목록, 강제 탈퇴
- 통계: recharts 사용하여 차트 구현
- 작업 완료 후 반드시 PROGRESS.md 기록
```

---

## 전체 완료 ✅
```
.claude/agents/
├── member-agent.md
├── post-agent.md
├── comment-agent.md
├── like-agent.md
├── file-agent.md
├── report-agent.md
├── block-agent.md
├── notification-agent.md
├── admin-agent.md
├── common-agent.md
├── auth-agent.md
├── board-agent.md
├── mypage-agent.md
└── admin-front-agent.md