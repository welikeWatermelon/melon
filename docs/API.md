# API 명세

## 공통

### Base URL
- 개발: http://localhost:8080
- 운영: 추후 결정

### 인증
- Authorization: Bearer {accessToken}
- PENDING 상태는 게시글/댓글 작성 불가

### 공통 응답 형식
```json
// 성공
{
  "success": true,
  "data": { }
}

// 실패
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

### 공통 에러 코드
| 코드 | 설명 |
|---|---|
| AUTH_001 | 인증 토큰 없음 |
| AUTH_002 | 토큰 만료 |
| AUTH_003 | 권한 없음 (PENDING 상태) |
| MEMBER_001 | 닉네임 중복 |
| POST_001 | 게시글 없음 |
| POST_002 | 수정 권한 없음 |
| FILE_001 | 파일 크기 초과 |
| FILE_002 | 허용되지 않는 파일 형식 |
| REPORT_001 | 이미 신고한 대상 |
| BLOCK_001 | 본인 차단 불가 |

### 페이지네이션
- 요청: page=0, size=20 (기본값)
- 응답:
```json
{
  "content": [],
  "hasNext": true,
  "totalCount": 100
}
```

---

## Auth (인증)

### 카카오 로그인
```
GET /api/auth/kakao/callback
Query: code (카카오 인가 코드)
Response: {
  accessToken: string,
  refreshToken: string,
  member: { id, nickname, therapyArea, role }
}
```

### 구글 로그인
```
GET /api/auth/google/callback
Query: code (구글 인가 코드)
Response: {
  accessToken: string,
  refreshToken: string,
  member: { id, nickname, therapyArea, role }
}
```

### AccessToken 재발급
```
POST /api/auth/refresh
Body: { refreshToken: string }
Response: { accessToken: string }
```

### 로그아웃
```
PATCH /api/auth/logout
Auth: 필요
Response: { }
```

---

## Member (회원)

### 내 정보 조회
```
GET /api/members/me
Auth: 필요
Response: {
  id: number,
  nickname: string,
  therapyArea: string,
  role: string
}
```

### 내 정보 수정
```
PATCH /api/members/me
Auth: 필요
Body: {
  nickname?: string,
  therapyArea?: string
}
Response: {
  id: number,
  nickname: string,
  therapyArea: string
}
```

### 회원 탈퇴
```
PATCH /api/members/me/delete
Auth: 필요
Response: { }
```

### 면허증 인증 신청
```
POST /api/members/me/license
Auth: 필요
Body: multipart/form-data { licenseImg: File }
Response: {
  id: number,
  status: string,
  createdAt: string
}
```

### 내 인증 상태 조회
```
GET /api/members/me/license
Auth: 필요
Response: {
  status: string,        // PENDING, APPROVED, REJECTED
  adminMemo?: string     // 거절 사유
}
```

---

## Post (게시글)

### 게시글 목록 조회
```
GET /api/posts
Auth: 필요
Query: {
  page?: number,         // 기본값 0
  size?: number,         // 기본값 20
  therapyArea?: string,  // 치료영역 필터
  keyword?: string       // 검색어
}
Response: {
  content: [{
    id: number,
    title: string,
    author: string,      // 닉네임 or "익명"
    therapyArea: string,
    likeCount: number,
    commentCount: number,
    viewCount: number,
    createdAt: string
  }],
  hasNext: boolean,
  totalCount: number
}
```

### 게시글 상세 조회
```
GET /api/posts/{postId}
Auth: 필요
Response: {
  id: number,
  title: string,
  content: string,       // TipTap JSON
  author: string,        // 닉네임 or "익명"
  isAnonymous: boolean,
  therapyArea: string,
  viewCount: number,
  likeCount: number,
  commentCount: number,
  isLiked: boolean,
  isScrapped: boolean,
  isMyPost: boolean,
  files: [{
    id: number,
    originalName: string,
    fileSize: number
  }],
  createdAt: string,
  updatedAt: string
}
```

### 게시글 작성
```
POST /api/posts
Auth: 필요 (MEMBER 이상)
Body: {
  title: string,
  content: string,       // TipTap JSON
  therapyArea?: string,
  isAnonymous: boolean,
  fileIds?: number[]
}
Response: { id: number }
```

### 게시글 수정
```
PATCH /api/posts/{postId}
Auth: 필요 (본인만)
Body: {
  title?: string,
  content?: string,
  therapyArea?: string,
  fileIds?: number[]
}
Response: { id: number }
```

### 게시글 삭제
```
PATCH /api/posts/{postId}/delete
Auth: 필요 (본인만)
Response: { }
```

### 내가 쓴 게시글 목록
```
GET /api/posts/my
Auth: 필요
Query: { page?, size? }
Response: { content: [...], hasNext, totalCount }
```

### 스크랩한 게시글 목록
```
GET /api/posts/scrapped
Auth: 필요
Query: { page?, size? }
Response: { content: [...], hasNext, totalCount }
```

### 스크랩 토글
```
POST /api/posts/{postId}/scraps
Auth: 필요
Response: { isScrapped: boolean }
```

---

## Comment (댓글)

### 댓글 목록 조회
```
GET /api/posts/{postId}/comments
Auth: 필요
Response: {
  comments: [{
    id: number,
    author: string,        // 닉네임 or "익명"
    content: string,       // 삭제된 경우 "삭제된 댓글입니다"
    isAnonymous: boolean,
    likeCount: number,
    isMyComment: boolean,
    createdAt: string,
    replies: [{
      id: number,
      author: string,
      content: string,
      isAnonymous: boolean,
      likeCount: number,
      isMyComment: boolean,
      createdAt: string
    }]
  }]
}
```

### 댓글 작성
```
POST /api/posts/{postId}/comments
Auth: 필요 (MEMBER 이상)
Body: {
  content: string,
  isAnonymous: boolean
}
Response: { id: number }
```

### 대댓글 작성
```
POST /api/posts/{postId}/comments/{commentId}/replies
Auth: 필요 (MEMBER 이상)
Body: {
  content: string,
  isAnonymous: boolean
}
Response: { id: number }
```

### 댓글 수정
```
PATCH /api/posts/{postId}/comments/{commentId}
Auth: 필요 (본인만)
Body: { content: string }
Response: { id: number }
```

### 댓글 삭제
```
PATCH /api/posts/{postId}/comments/{commentId}/delete
Auth: 필요 (본인만)
Response: { }
```

### 내가 쓴 댓글 목록
```
GET /api/members/me/comments
Auth: 필요
Query: { page?, size? }
Response: {
  content: [{
    id: number,
    content: string,
    postId: number,
    postTitle: string,
    createdAt: string
  }],
  hasNext: boolean,
  totalCount: number
}
```

---

## Like (좋아요)

### 좋아요 토글
```
POST /api/likes
Auth: 필요
Body: {
  targetType: string,    // POST, COMMENT
  targetId: number
}
Response: {
  isLiked: boolean,
  likeCount: number
}
```

---

## File (파일)

### 파일 업로드
```
POST /api/files/upload
Auth: 필요
Body: multipart/form-data { file: File }
허용 형식: pdf, hwp, jpg, jpeg, png, gif
최대 크기: 10MB
Response: {
  fileId: number,
  originalName: string,
  fileSize: number
}
```

### 파일 다운로드
```
GET /api/files/{fileId}/download
Auth: 필요
Response: presigned URL로 redirect
```

---

## Report (신고)

### 신고 접수
```
POST /api/reports
Auth: 필요
Body: {
  targetType: string,    // POST, COMMENT
  targetId: number,
  reason: string
}
Response: { }
```

---

## Block (차단)

### 차단 토글
```
POST /api/blocks/{memberId}
Auth: 필요
Response: { isBlocked: boolean }
```

### 차단 목록 조회
```
GET /api/blocks
Auth: 필요
Response: {
  blockedMembers: [{
    id: number,
    nickname: string
  }]
}
```

---

## Notification (알림)

### SSE 연결
```
GET /api/notifications/subscribe
Auth: 필요
Response: text/event-stream
Event: {
  id: number,
  type: string,          // COMMENT_ON_POST, REPLY_ON_COMMENT, LIKE_ON_POST, LIKE_ON_COMMENT
  targetType: string,
  targetId: number,
  message: string,
  createdAt: string
}
```

### 알림 목록 조회
```
GET /api/notifications
Auth: 필요
Query: { page?, size? }
Response: {
  content: [{
    id: number,
    type: string,
    targetType: string,
    targetId: number,
    message: string,
    isRead: boolean,
    createdAt: string
  }],
  unreadCount: number,
  hasNext: boolean
}
```

### 전체 읽음 처리
```
PATCH /api/notifications/read-all
Auth: 필요
Response: { }
```

### 개별 읽음 처리
```
PATCH /api/notifications/{id}/read
Auth: 필요
Response: { }
```

---

## Admin (관리자)

### 인증 신청 목록
```
GET /api/admin/licenses
Auth: ADMIN
Query: {
  status?: string,       // PENDING, APPROVED, REJECTED
  page?, size?
}
Response: {
  content: [{
    id: number,
    memberId: number,
    nickname: string,
    licenseImgUrl: string,
    status: string,
    createdAt: string
  }],
  hasNext: boolean,
  totalCount: number
}
```

### 인증 승인/거절
```
PATCH /api/admin/licenses/{id}
Auth: ADMIN
Body: {
  status: string,        // APPROVED, REJECTED
  adminMemo?: string     // 거절 시 필수
}
Response: { }
```

### 신고 목록
```
GET /api/admin/reports
Auth: ADMIN
Query: { status?: string, page?, size? }
Response: {
  content: [{
    id: number,
    reporterId: number,
    targetType: string,
    targetId: number,
    reason: string,
    status: string,
    createdAt: string
  }],
  hasNext: boolean,
  totalCount: number
}
```

### 신고 처리
```
PATCH /api/admin/reports/{id}
Auth: ADMIN
Body: {
  action: string         // HIDE_POST, HIDE_COMMENT, DISMISS
}
Response: { }
```

### 회원 목록
```
GET /api/admin/members
Auth: ADMIN
Query: { role?: string, page?, size? }
Response: {
  content: [{
    id: number,
    nickname: string,
    therapyArea: string,
    role: string,
    createdAt: string
  }],
  hasNext: boolean,
  totalCount: number
}
```

### 회원 제재
```
PATCH /api/admin/members/{id}
Auth: ADMIN
Body: {
  action: string         // FORCE_DELETE
}
Response: { }
```

### 통계 조회
```
GET /api/admin/stats
Auth: ADMIN
Response: {
  wau: number,           // 주간 활성 유저
  mau: number,           // 월간 활성 유저
  weeklyPostCount: number,
  uploaderRatio: number, // 게시글 작성자 비율 (%)
  retentionRate: number  // 30일 잔존율 (%)
}
```