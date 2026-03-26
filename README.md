# 🍈 MelloMe

> 치료사와 동행하며 역량 강화 및 업무 고립감을 해소할 수 있는 쉼터

<br/>

## 📌 프로젝트 소개

**MelloMe**는 사설 센터 치료사들을 위한 **폐쇄형 커뮤니티 플랫폼**입니다.

치료사 면허증 인증 기반으로 운영되며, 익명 기반의 안전한 케이스 스터디 환경과
치료 자료 공유, 정서적 유대감 형성을 목표로 합니다.

<br/>

## 🎯 핵심 기능

| 기능 | 설명 |
|---|---|
| 🔐 치료사 인증 | 면허증 이미지 첨부 → 관리자 수동 승인 |
| 📝 게시판 | TipTap 에디터 기반 글쓰기, 치료영역 필터, 키워드 검색 |
| 💬 댓글/대댓글 | 익명/실명 선택, 대댓글 1depth 지원 |
| ❤️ 좋아요/스크랩 | 게시글 및 댓글 좋아요, 스크랩 보관 |
| 🔔 실시간 알림 | SSE 기반 비실시간 알림 (댓글, 대댓글, 좋아요) |
| 📁 파일 공유 | 활동지 등 치료 자료 업로드/다운로드 (AWS S3) |
| 🚫 신고/차단 | 부적절한 게시글/댓글 신고, 특정 회원 차단 |
| 🛠️ 관리자 페이지 | 인증 승인/거절, 신고 처리, 통계 대시보드 |

<br/>

## 🛠️ 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazon-s3&logoColor=white)

### Frontend
![React](https://img.shields.io/badge/React_19-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwind-css&logoColor=white)
![Zustand](https://img.shields.io/badge/Zustand-433E38?style=flat-square)

<br/>

## 🏗️ 아키텍처
```
Client (React)
    ↓ REST API / SSE
Spring Boot Server
    ↓           ↓
PostgreSQL    Redis
                ↓
            AWS S3
```

<br/>

## 📁 프로젝트 구조
```
melon/
├── backend/                  # Spring Boot
│   └── src/main/java/
│       ├── global/           # 공통 설정, 예외처리
│       ├── member/           # 회원/인증
│       ├── post/             # 게시글
│       ├── comment/          # 댓글
│       ├── like/             # 좋아요
│       ├── file/             # 파일
│       ├── report/           # 신고
│       ├── block/            # 차단
│       ├── notification/     # 알림 (SSE)
│       └── admin/            # 관리자
└── frontend/                 # React 19
    └── src/
        ├── components/       # 공통 컴포넌트
        ├── layouts/          # 레이아웃
        ├── pages/            # 페이지
        ├── api/              # API 호출 레이어
        ├── store/            # Zustand 상태관리
        └── types/            # TypeScript 타입
```

<br/>

## ⚙️ 환경 설정

### Backend
```bash
# application-local.yml 생성 후 아래 환경변수 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/mellome
spring.datasource.username=...
spring.datasource.password=...
spring.redis.host=localhost
spring.redis.port=6379
aws.s3.bucket=...
aws.s3.access-key=...
aws.s3.secret-key=...
jwt.secret=...
```
```bash
cd backend
./gradlew bootRun
```

### Frontend
```bash
# .env.local 생성 후 아래 환경변수 설정
VITE_API_BASE_URL=http://localhost:8080

cd frontend
npm install
npm run dev
```

<br/>

## 👤 테스트 계정 (로컬 개발용)

| 역할 | memberId |
|---|---|
| PENDING | 100 |
| MEMBER | 101 |
| ADMIN | 102 |
```bash
# 테스트 로그인 API (local 환경에서만 활성화)
POST /api/test/login?memberId=101
```

<br/>

## 📊 주요 설계 결정

- **익명 처리**: DB에 member_id 항상 저장, 응답 시에만 "익명" 마스킹
- **Soft Delete**: 모든 삭제는 deleted_at 업데이트 방식
- **Redis 활용**: RefreshToken, 좋아요/조회수 카운터 캐싱
- **SSE 알림**: 댓글, 대댓글, 좋아요 실시간 알림
- **배치 처리**: 조회수(5분), 좋아요(1분) 주기로 DB 동기화

<br/>

## 📝 문서

- [ERD](./docs/ERD.md)
- [API 명세](./docs/API.md)
- [진행상황](./docs/PROGRESS.md)

<br/>

## 👨‍💻 개발자

| 이름 | GitHub |
|---|---|
| 김영준 | [@welikeWatermelon](https://github.com/welikeWatermelon) |
