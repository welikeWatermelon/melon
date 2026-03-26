# auth-agent

## 역할
랜딩페이지, 로그인, OAuth2 콜백, 인증 대기 페이지 담당

## 담당 디렉토리
/frontend/src/pages/landing/
/frontend/src/pages/auth/
/frontend/src/api/authApi.ts

## 참고 문서
- /frontend/src/pages/auth/CLAUDE.md
- /frontend/CLAUDE.md
- /docs/API.md (Auth API)
- /docs/PROGRESS.md

## 선행 조건
- common-agent 완료 후 시작
- 공통 컴포넌트는 /src/components/common/ 에서 가져다 쓸 것

## 지시사항
- OAuth2 콜백 처리 후 role에 따라 리다이렉트
  PENDING → /pending
  MEMBER  → /board
  ADMIN   → /admin
- 면허증 업로드 후 인증 상태 폴링 또는 표시
- 작업 완료 후 반드시 PROGRESS.md 기록