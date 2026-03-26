# Member 도메인

## 담당 범위
- Member (회원 정보)
- License (면허증 인증)
- OAuth2 (카카오/구글 소셜 로그인)
- JWT (AccessToken, RefreshToken)
- Spring Security 설정 전담

## 담당 디렉토리
/src/main/java/.../member/
/src/main/java/.../global/config/SecurityConfig.java
/src/main/java/.../global/config/OAuth2Config.java

## 주의사항
- Security 설정은 이 에이전트만 담당
- 다른 에이전트는 SecurityConfig 건드리지 말 것
- JWT 필터, OAuth2 핸들러 전부 여기서 작성

## 구현 목록
1. OAuth2 소셜 로그인 (카카오, 구글)
2. JWT AccessToken (1시간) + RefreshToken (30일) 발급
3. RefreshToken Redis 저장/조회/삭제
4. AccessToken 재발급
5. 로그아웃
6. 내 정보 조회/수정
7. 회원 탈퇴 (soft delete)
8. 면허증 이미지 S3 업로드 후 인증 신청
9. 내 인증 상태 조회

## ERD
Member
- id, provider(KAKAO/GOOGLE), provider_id
- nickname(UNIQUE), therapy_area, role(PENDING/MEMBER/ADMIN)
- created_at, updated_at, deleted_at

License
- id, member_id, license_img_url(S3)
- status(PENDING/APPROVED/REJECTED)
- admin_memo, reviewed_by, reviewed_at, created_at

## 비즈니스 규칙
- 신규 가입 시 role = PENDING
- 면허 승인 시 role = MEMBER로 변경
- PENDING 상태는 게시글/댓글 작성 불가
- 닉네임 중복 불가 (UNIQUE 제약)
- 회원 탈퇴 시 deleted_at 업데이트 (soft delete)

## JWT 규칙
- AccessToken: Authorization Bearer 헤더
- RefreshToken: Redis key = refresh:{memberId} TTL 30일
- 재발급 시 RefreshToken도 rotate (보안)

## API
POST  /api/auth/kakao/callback
POST  /api/auth/google/callback
POST  /api/auth/refresh
PATCH /api/auth/logout
GET   /api/members/me
PATCH /api/members/me
PATCH /api/members/me/delete
POST  /api/members/me/license
GET   /api/members/me/license

## 단위테스트 필수 항목
- OAuth2 신규 가입 / 기존 로그인 분기
- PENDING 상태 접근 차단
- RefreshToken rotate
- 닉네임 중복 예외
- 면허 신청 중복 예외