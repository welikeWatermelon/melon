# Admin 도메인

## 담당 범위
- 면허 인증 승인/거절
- 신고 처리
- 회원 관리
- 통계

## 담당 디렉토리
/src/main/java/.../admin/

## 구현 목록
1. 면허 인증 신청 목록 조회
2. 면허 승인/거절 (Member.role 변경)
3. 신고 목록 조회
4. 신고 처리 (게시글/댓글 숨김, 기각)
5. 회원 목록 조회
6. 회원 강제 탈퇴
7. 통계 조회

## 비즈니스 규칙
- ADMIN role만 접근 가능 (Security에서 차단)
- 면허 승인 시 Member.role = PENDING → MEMBER
- 면허 거절 시 admin_memo 필수
- 신고 처리 액션:
  HIDE_POST    → Post.status = HIDDEN
  HIDE_COMMENT → Comment.status = DELETED
  DISMISS      → 신고 기각
- 강제 탈퇴 시 soft delete

## 통계 계산
- WAU: 최근 7일 로그인한 unique member 수
- MAU: 최근 30일 로그인한 unique member 수
- 게시글 생성률: 최근 7일 게시글 수 / 전체 회원 수
- Uploader 비율: 최근 30일 게시글 작성자 수 / 전체 MEMBER 수

## API
GET   /api/admin/licenses
PATCH /api/admin/licenses/{id}
GET   /api/admin/reports
PATCH /api/admin/reports/{id}
GET   /api/admin/members
PATCH /api/admin/members/{id}
GET   /api/admin/stats

## 단위테스트 필수 항목
- ADMIN 권한 체크
- 면허 승인 시 role 변경
- 신고 처리 액션별 분기
- 통계 계산 로직