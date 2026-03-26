# 프론트 Auth 에이전트

## 담당 범위
랜딩페이지, 로그인, OAuth2 콜백, 인증 대기 페이지

## 담당 디렉토리
/src/pages/landing/
/src/pages/auth/
/src/api/authApi.ts

## 선행 조건
- common-agent 완료 후 시작
- 공통 컴포넌트는 /src/components/common/ 에서 가져다 쓸 것

## 구현 목록

### 랜딩 페이지 (/landing)
- GNB (로그인 버튼)
- 히어로 섹션 + CTA 버튼 (로그인, 치료사 인증)
- 커뮤니티 설명 섹션
- 라스트 섹션
- 푸터

### 로그인 페이지 (/login)
- 카카오 로그인 버튼
- 구글 로그인 버튼
- 소셜 로그인 버튼 클릭 시 OAuth2 URL로 리다이렉트

### OAuth2 콜백 처리 (/auth/callback)
- URL에서 code 파라미터 추출
- 백엔드 /api/auth/{provider}/callback 호출
- AccessToken 수신 → authStore 저장
- role에 따라 리다이렉트
  PENDING → /pending
  MEMBER  → /board
  ADMIN   → /admin

### 인증 대기 페이지 (/pending)
- 면허증 이미지 업로드 UI
- 업로드 후 인증 신청 상태 표시
  PENDING  → "검토 중입니다"
  REJECTED → "거절 사유 + 재신청 가능"
  APPROVED → /board 리다이렉트

## API 연동
- GET  /api/auth/kakao/callback
- GET  /api/auth/google/callback
- POST /api/auth/refresh
- PATCH /api/auth/logout
- POST /api/members/me/license
- GET  /api/members/me/license