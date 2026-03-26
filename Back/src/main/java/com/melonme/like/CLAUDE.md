# Like 도메인

## 담당 범위
- Like (게시글/댓글 좋아요 통합)

## 담당 디렉토리
/src/main/java/.../like/

## 구현 목록
1. 좋아요 토글 (POST/COMMENT 통합)
2. Redis 카운터 관리
3. 배치: Redis → PostgreSQL 동기화
4. 좋아요 시 알림 이벤트 발행

## ERD
Like
- id, member_id
- target_type(POST/COMMENT), target_id
- created_at
- UNIQUE (member_id, target_type, target_id)

## Redis 규칙
- 게시글 좋아요: like:post:{postId}
- 댓글 좋아요:   like:comment:{commentId}
- 좋아요 추가 시 Redis +1, DB insert
- 좋아요 취소 시 Redis -1, DB delete
- 1분마다 배치로 PostgreSQL 반영
  (Post.like_count, Comment.like_count 업데이트)

## 비즈니스 규칙
- 본인 게시글/댓글에도 좋아요 가능
- 중복 좋아요 불가 (UNIQUE 제약)
- 토글 방식 (있으면 취소, 없으면 추가)
- 좋아요 추가 시 ApplicationEvent 발행
  → Notification 도메인에서 수신

## API
POST /api/likes
Body: { targetType, targetId }
Return: { isLiked, likeCount }

## 단위테스트 필수 항목
- 좋아요 토글 (추가/취소)
- 중복 좋아요 예외
- Redis 카운터 증감
- 알림 이벤트 발행 확인
- 배치 DB 동기화