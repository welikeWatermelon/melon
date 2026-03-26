# member-agent

## 역할
회원/인증 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../member/
/backend/src/main/java/.../global/config/SecurityConfig.java
/backend/src/main/java/.../global/config/OAuth2Config.java

## 참고 문서
- /backend/src/main/java/.../member/CLAUDE.md
- /docs/ERD.md (Member, License 테이블)
- /docs/API.md (Auth, Member API)
- /docs/PROGRESS.md

## 지시사항
- Security 설정 전담, 다른 에이전트 SecurityConfig 수정 금지
- OAuth2 카카오/구글 로그인 구현
- JWT AccessToken(1시간) + RefreshToken(30일) 발급
- RefreshToken Redis 저장 (key: refresh:{memberId})
- 작업 완료 후 반드시 PROGRESS.md 기록