# 치료사 커뮤니티 프로젝트

## 프로젝트 개요
치료사 전용 폐쇄형 커뮤니티 플랫폼
- 타겟: 사설 센터 근무 20/30대 여성 치료사
- 웹 우선 배포 (모바일 앱 제외)
- 면허증 인증 기반 폐쇄형 커뮤니티

## 기술 스택
- 백엔드: Spring Boot + Java + PostgreSQL + Redis + S3
- 프론트: React 19 + TypeScript + Vite + Tailwind + Zustand
- 상세 규칙 → Back/CLAUDE.md, Front/CLAUDE.md 참고

## 프로젝트 구조
```
프로젝트/
├── CLAUDE.md
├── docs/
│   ├── PROGRESS.md
│   ├── ERD.md
│   └── API.md
├── .claude/
│   └── settings.json
├── Back/
│   └── CLAUDE.md
└── Front/
    └── CLAUDE.md
```

## 에이전트 구성
- 상세 역할 정의 → Back/CLAUDE.md, Front/CLAUDE.md 참고

### 실행 순서
```
백엔드 9개 에이전트 → 전부 병렬 실행
프론트 common-agent → 먼저 완료
프론트 나머지 4개  → 병렬 실행
```

## PROGRESS.md
- 경로: /docs/PROGRESS.md
- 모든 에이전트는 작업 완료 후 반드시 기록

## 작업 전 필수 규칙
- 구현 전 비즈니스 결정사항 파악
- 판단 필요한 항목은 사용자에게 질문
- 선택지와 이유 함께 제시
- 한 번에 몰아서 질문


## 참고 문서
- ERD: /docs/ERD.md
- API 명세: /docs/API.md
- 진행상황: /docs/PROGRESS.md


---

## 정리하면
```
루트 CLAUDE.md        개요, 구조, 에이전트 순서, 작업 규칙만
Back/CLAUDE.md        Spring Boot 공통 규칙 상세
Front/CLAUDE.md       React 공통 규칙 상세
각 도메인 CLAUDE.md   도메인별 구현 상세
```