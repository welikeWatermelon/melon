# Comment 도메인

## 담당 범위
- Comment (댓글, 대댓글)

## 담당 디렉토리
/src/main/java/.../comment/

## 구현 목록
1. 댓글 목록 조회 (대댓글 중첩 반환)
2. 댓글 작성
3. 대댓글 작성
4. 댓글 수정
5. 댓글 삭제 (soft delete)
6. 내가 쓴 댓글 목록

## ERD
Comment
- id, post_id, member_id
- parent_id (nullable, 대댓글)
- content, is_anonymous, like_count
- status(ACTIVE/DELETED)
- created_at, updated_at, deleted_at

## 비즈니스 규칙
- is_anonymous = true 이면 응답에서 author = "익명"
- DB에는 member_id 항상 저장
- 삭제된 댓글은 "삭제된 댓글입니다" 텍스트로 반환
  (대댓글이 있으면 댓글 자체는 유지, 없으면 숨김)
- 대댓글은 1depth만 허용 (대댓글의 대댓글 없음)
- 댓글 작성 시 Post.comment_count +1
- 댓글 삭제 시 Post.comment_count -1
- 차단한 회원의 댓글은 "차단한 사용자의 댓글입니다"로 반환

## 응답 구조
```json
{
  "comments": [
    {
      "id": 1,
      "author": "닉네임 or 익명",
      "content": "댓글 내용",
      "isAnonymous": false,
      "likeCount": 3,
      "createdAt": "",
      "replies": [
        {
          "id": 2,
          "author": "익명",
          "content": "대댓글 내용",
          ...
        }
      ]
    }
  ]
}
```

## API
GET   /api/posts/{postId}/comments
POST  /api/posts/{postId}/comments
PATCH /api/posts/{postId}/comments/{commentId}
PATCH /api/posts/{postId}/comments/{commentId}/delete
POST  /api/posts/{postId}/comments/{commentId}/replies
GET   /api/members/me/comments

## 단위테스트 필수 항목
- 익명 댓글 작성자 마스킹
- 삭제된 댓글 텍스트 처리 (대댓글 유무에 따라 분기)
- 대댓글 depth 제한 (2depth 시도 시 예외)
- 차단 회원 댓글 마스킹
- comment_count 증감
- PENDING 상태 작성 불가