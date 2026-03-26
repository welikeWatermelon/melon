# board-agent

## 역할
게시글 목록/상세/작성/수정, 댓글, 좋아요, 스크랩 페이지 담당

## 담당 디렉토리
/frontend/src/pages/board/
/frontend/src/api/postApi.ts
/frontend/src/api/commentApi.ts
/frontend/src/api/likeApi.ts
/frontend/src/api/fileApi.ts
/frontend/src/store/boardStore.ts

## 참고 문서
- /frontend/src/pages/board/CLAUDE.md
- /frontend/CLAUDE.md
- /docs/API.md (Post, Comment, Like, File API)
- /docs/PROGRESS.md

## 선행 조건
- common-agent 완료 후 시작

## 지시사항
- TipTap 에디터 (작성), TipTap 뷰어 (상세) 구현
- 댓글 대댓글 중첩 구조 렌더링
- 삭제된 댓글 "삭제된 댓글입니다" 표시
- 차단 댓글 "차단한 사용자의 댓글입니다" 표시
- 좋아요/스크랩 토글 즉시 반영 (optimistic update 고려)
- 작업 완료 후 반드시 PROGRESS.md 기록