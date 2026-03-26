# 프론트 Admin 에이전트

## 담당 범위
관리자 페이지 전체

## 담당 디렉토리
/src/pages/admin/
/src/api/adminApi.ts

## 선행 조건
- common-agent 완료 후 시작
- AdminLayout 사용 (common-agent가 만들어놓은 것)
- ADMIN role만 접근 가능 (AdminRoute 가드)

## 구현 목록

### 인증 관리 (/admin/licenses)
- 신청 목록 테이블 (닉네임, 신청일, 상태)
- 면허증 이미지 모달로 보기
- 승인 버튼
- 거절 버튼 + 거절 사유 입력 모달

### 신고 관리 (/admin/reports)
- 신고 목록 테이블
- 신고 대상 내용 확인
- 처리 액션:
    - 게시글/댓글 숨김
    - 신고 기각
- 처리 완료 시 status = PROCESSED

### 회원 관리 (/admin/members)
- 회원 목록 테이블 (닉네임, 치료영역, role, 가입일)
- 강제 탈퇴 버튼

### 통계 대시보드 (/admin/stats)
- WAU, MAU 수치
- 게시글 생성률
- Uploader 비율
- 간단한 차트 (recharts 사용)

## API 연동
- GET /api/admin/licenses
- PATCH /api/admin/licenses/{id}
- GET /api/admin/reports
- PATCH /api/admin/reports/{id}
- GET /api/admin/members
- PATCH /api/admin/members/{id}
- GET /api/admin/stats
```

---

## 전체 완료 ✅
```
Main CLAUDE.md          ← 수정 완료
백엔드 (9개)            ← 완료
프론트 (5개)
├── common-agent        ← 완료
├── auth-agent          ← 완료
├── board-agent         ← 완료
├── mypage-agent        ← 완료
└── admin-agent         ← 완료