# Post 도메인

## 담당 범위
- Post (게시글 CRUD)
- Scrap (스크랩)

## 담당 디렉토리
/src/main/java/.../post/

## 구현 목록
1. 게시글 목록 조회 (페이지네이션, 치료영역 필터, 키워드 검색)
2. 게시글 상세 조회 (조회수 Redis +1)
3. 게시글 작성 (TipTap JSON, 치료영역, 익명여부, 파일첨부)
4. 게시글 수정
5. 게시글 삭제 (soft delete)
6. 내가 쓴 게시글 목록
7. 스크랩 토글
8. 스크랩한 게시글 목록

## ERD
Post
- id, member_id, title, content(TEXT)
- therapy_area, is_anonymous
- view_count, like_count, comment_count
- status(ACTIVE/HIDDEN/DELETED)
- created_at, updated_at, deleted_at

Scrap
- id, member_id, post_id, created_at
- UNIQUE (member_id, post_id)

## 비즈니스 규칙
- is_anonymous = true 이면 응답에서 author = "익명"
- DB에는 member_id 항상 저장
- 본인 게시글은 익명이어도 본인 확인 가능
- 조회수: Redis view:post:{postId} +1, 5분마다 배치로 DB 반영
- like_count, comment_count는 역정규화 컬럼 (각 도메인에서 업데이트)
- 차단한 회원의 게시글은 목록에서 제외
- 삭제된 게시글 상세 조회 시 404 반환

## 검색 규칙
- 제목 + 본문 키워드 검색 (PostgreSQL ILIKE)
- 치료영역 필터 (단일 선택)
- 정렬: 최신순 기본

## API
GET   /api/posts
GET   /api/posts/{postId}
POST  /api/posts
PATCH /api/posts/{postId}
PATCH /api/posts/{postId}/delete
GET   /api/posts/my
GET   /api/posts/scrapped
POST  /api/posts/{postId}/scraps

## 단위테스트 필수 항목
- 익명 게시글 작성자 마스킹
- 차단 회원 게시글 필터링
- 조회수 Redis 증가
- 스크랩 토글 (추가/취소)
- 권한 없는 수정/삭제 예외
- PENDING 상태 작성 불가