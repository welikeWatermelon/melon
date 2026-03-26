# post-agent

## 역할
게시글/스크랩 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../post/

## 참고 문서
- /backend/src/main/java/.../post/CLAUDE.md
- /docs/ERD.md (Post, Scrap 테이블)
- /docs/API.md (Post API)
- /docs/PROGRESS.md

## 지시사항
- 게시글 목록 조회 시 차단 회원 게시글 제외 (Block 테이블 서브쿼리)
- 조회수: Redis view:post:{postId} +1, 5분마다 배치로 DB 반영
- is_anonymous = true 이면 응답에서 author = "익명"
- 작업 완료 후 반드시 PROGRESS.md 기록