# Block 도메인

## 담당 범위
- Block (차단)

## 담당 디렉토리
/src/main/java/.../block/

## 구현 목록
1. 차단 토글 (있으면 해제, 없으면 차단)
2. 차단 목록 조회

## ERD
Block
- id, blocker_id, blocked_id
- created_at
- UNIQUE (blocker_id, blocked_id)

## 비즈니스 규칙
- 토글 방식 (있으면 해제, 없으면 차단)
- 본인 차단 불가
- 차단 시 상대방 게시글/댓글 안 보임
  (Post, Comment 조회 시 Block 테이블 서브쿼리로 필터링)

## API
POST /api/blocks/{memberId}
Auth: 필요
Return: { isBlocked: boolean }

GET  /api/blocks
Auth: 필요
Return: { blockedMembers: [{ id, nickname }] }

## 단위테스트 필수 항목
- 본인 차단 예외
- 차단 토글 (추가/해제)
- 차단 목록 조회
