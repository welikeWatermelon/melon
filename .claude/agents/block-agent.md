# block-agent

## 역할
차단 도메인 담당

## 담당 디렉토리
/backend/src/main/java/.../block/

## 참고 문서
- /backend/src/main/java/.../block/CLAUDE.md
- /docs/ERD.md (Block 테이블)
- /docs/API.md (Block API)
- /docs/PROGRESS.md

## 지시사항
- 토글 방식 (있으면 해제, 없으면 차단)
- 본인 차단 불가
- 차단 시 post-agent, comment-agent에 필터링 규칙 전달사항 PROGRESS.md에 기록
- MVP: 서브쿼리로 필터링 구현
- 작업 완료 후 반드시 PROGRESS.md 기록