# 프론트엔드 공통 규칙

## 기술 스택
- React 19 + TypeScript
- Vite
- Tailwind CSS
- Zustand (상태관리)
- React Router (라우팅)

## 디렉토리 구조
```
src/
├── components/
│   └── common/          ← 공통 컴포넌트 (수정 금지)
├── layouts/             ← 레이아웃 (수정 금지)
├── pages/
│   ├── landing/
│   ├── auth/
│   ├── board/
│   ├── mypage/
│   └── admin/
├── api/
│   ├── instance.ts      ← Axios 인스턴스 (수정 금지)
│   ├── authApi.ts
│   ├── postApi.ts
│   ├── commentApi.ts
│   ├── likeApi.ts
│   ├── fileApi.ts
│   ├── notificationApi.ts
│   ├── blockApi.ts
│   └── adminApi.ts
├── store/
│   ├── authStore.ts         ← 수정 금지
│   ├── notificationStore.ts
│   └── boardStore.ts
├── types/               ← 공통 타입 (수정 금지)
├── router/              ← 라우팅 (수정 금지)
└── styles/
```

---

## Common Agent 담당 (최우선 완료)

### 중요
- common-agent가 먼저 완료되어야 나머지 4개 에이전트 시작 가능
- 아래 항목들은 common-agent 전담
- 다른 에이전트는 수정 금지, 가져다 쓰기만 할 것
- 추가 공통 컴포넌트 필요하면 직접 만들지 말고
  PROGRESS.md에 필요한 컴포넌트 기록

### 공통 컴포넌트 (/src/components/common/)
- Button (variant: primary, secondary, ghost, danger)
- Input, Textarea
- Modal
- Toast (success, error, info)
- Spinner
- Pagination
- Avatar
- Badge (치료영역 태그)
- EmptyState
- ErrorBoundary
- Dropdown

### 레이아웃 (/src/layouts/)
- GNB
  - 비로그인: 로그인 버튼
  - PENDING: 인증 안내
  - MEMBER: 알림 아이콘(뱃지), 마이페이지, 로그아웃
  - ADMIN: 관리자 메뉴 추가
- Footer
- MainLayout (GNB + 콘텐츠 + Footer)
- AdminLayout

### 라우팅 (/src/router/)
- PublicRoute: 랜딩, 로그인 (비로그인 접근 가능)
- PendingRoute: 인증 대기 페이지 (PENDING만)
- PrivateRoute: 게시판, 마이페이지 (MEMBER 이상)
- AdminRoute: 관리자 페이지 (ADMIN만)
- 로그인 후 role에 따라 자동 리다이렉트
  PENDING → /pending
  MEMBER  → /board
  ADMIN   → /admin

### Axios 인스턴스 (/src/api/instance.ts)
- baseURL 환경변수로 주입 (VITE_API_BASE_URL)
- Authorization Bearer 헤더 자동 주입
- 401 시 자동 토큰 재발급 interceptor
- 재발급 실패 시 로그인 페이지 이동
- 공통 에러 Toast 처리

### TypeScript 타입 (/src/types/)
```typescript
// 공통 응답
interface ApiResponse<T> {
  success: boolean
  data: T
}

interface PageResponse<T> {
  content: T[]
  hasNext: boolean
  totalCount: number
}

// 도메인 타입
interface Member {
  id: number
  nickname: string
  therapyArea: string
  role: 'PENDING' | 'MEMBER' | 'ADMIN'
}

interface Post {
  id: number
  title: string
  content: string
  author: string
  isAnonymous: boolean
  therapyArea: string
  viewCount: number
  likeCount: number
  commentCount: number
  isLiked: boolean
  isScrapped: boolean
  isMyPost: boolean
  files: File[]
  createdAt: string
}

interface Comment {
  id: number
  author: string
  content: string
  isAnonymous: boolean
  likeCount: number
  isMyComment: boolean
  createdAt: string
  replies: Comment[]
}

interface Notification {
  id: number
  type: 'COMMENT_ON_POST' | 'REPLY_ON_COMMENT' | 'LIKE_ON_POST' | 'LIKE_ON_COMMENT'
  targetType: 'POST' | 'COMMENT'
  targetId: number
  message: string
  isRead: boolean
  createdAt: string
}
```

### Zustand authStore (/src/store/authStore.ts)
```typescript
interface AuthStore {
  accessToken: string | null
  member: Member | null
  setAuth: (token: string, member: Member) => void
  clearAuth: () => void
}
```

### 디자인 시스템 (tailwind.config.ts)
- 타겟: 20/30대 여성 치료사
- 따뜻하고 아기자기한 느낌
- 폰트: Pretendard
- 브랜드 컬러 커스텀 정의
- 반응형: 모바일 우선 (sm → md → lg)

---

## 전체 에이전트 공통 규칙

### 컴포넌트 작성 규칙
- 함수형 컴포넌트만 사용
- props 타입은 interface로 정의
- 파일명: PascalCase (PostCard.tsx)
- 컴포넌트당 파일 하나

### API 연동 규칙
- 모든 API 호출은 /src/api/ 에서만
- instance.ts 반드시 사용 (직접 axios 호출 금지)
- API 함수명: {동사}{명사} (fetchPosts, createPost)
- 에러 처리: try/catch + Toast 컴포넌트

### Zustand 규칙
- 스토어 파일명: {name}Store.ts
- 액션은 스토어 내부에 정의
- 전역 상태 최소화 (서버 상태는 API 직접 호출)

### Tailwind 규칙
- 커스텀 컬러는 tailwind.config.ts에서만 정의
- 인라인 스타일 사용 금지
- 공통 클래스 조합은 컴포넌트로 추출

### TypeScript 규칙
- any 사용 금지
- 모든 props, API 응답에 타입 정의
- 공통 타입은 /src/types/ 에서 가져다 쓸 것

### SSE 연결 규칙
- 로그인 성공 시 SSE 구독 시작
- 로그아웃 시 SSE 연결 종료
- 알림 수신 시 notificationStore 업데이트
- 연결 끊기면 3초 후 재연결 시도