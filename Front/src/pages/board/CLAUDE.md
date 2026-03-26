# 프론트 Board 에이전트

## 담당 범위
게시글 목록/상세/작성/수정, 댓글, 대댓글, 좋아요, 스크랩

## 담당 디렉토리
/src/pages/board/
/src/api/postApi.ts
/src/api/commentApi.ts
/src/api/likeApi.ts
/src/api/fileApi.ts
/src/store/boardStore.ts

## 선행 조건
- common-agent 완료 후 시작

## 구현 목록

### 게시글 목록 (/board)
- 치료영역 필터 탭
- 키워드 검색
- 게시글 카드 리스트
- 페이지네이션
- 글쓰기 버튼 (MEMBER 이상)

### 게시글 상세 (/board/{postId})
- 제목, 작성자(익명/닉네임), 날짜
- TipTap 뷰어 (읽기 전용)
- 첨부 파일 다운로드
- 좋아요 버튼 (토글)
- 스크랩 버튼 (토글)
- 신고 버튼
- 수정/삭제 버튼 (본인만)
- 댓글 목록
- 댓글 작성

### 게시글 작성 (/board/write)
- 제목 입력
- TipTap 에디터
- 치료영역 선택
- 익명 여부 토글
- 파일 첨부 (다중 업로드)
- 임시저장 고려 (선택)

### 게시글 수정 (/board/{postId}/edit)
- 작성 페이지와 동일한 UI
- 기존 데이터 불러와서 수정

### 댓글 컴포넌트
- 댓글 목록 (대댓글 중첩)
- 댓글 작성 (익명 토글)
- 대댓글 작성
- 댓글 좋아요
- 삭제된 댓글 "삭제된 댓글입니다" 표시
- 차단 댓글 "차단한 사용자의 댓글입니다" 표시

## API 연동
- GET/POST/PATCH /api/posts
- GET/POST/PATCH /api/posts/{postId}/comments
- POST /api/likes
- POST /api/posts/{postId}/scraps
- POST /api/files/upload
- GET  /api/files/{fileId}/download
- POST /api/reports