# ERD 설계

## 테이블 목록
- Member (회원)
- License (면허 인증)
- Post (게시글)
- Comment (댓글/대댓글)
- File (파일)
- Like (좋아요)
- Scrap (스크랩)
- Report (신고)
- Block (차단)
- Notification (알림)

---

## Member (회원)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| provider | VARCHAR | NOT NULL | KAKAO, GOOGLE |
| provider_id | VARCHAR | NOT NULL | 소셜 고유 ID |
| nickname | VARCHAR | UNIQUE NOT NULL | |
| therapy_area | VARCHAR | | 작업, 언어, 인지, 놀이, 기타 |
| role | VARCHAR | NOT NULL | PENDING, MEMBER, ADMIN |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | | soft delete |

## License (면허 인증)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| member_id | BIGINT | FK → Member | |
| license_img_url | VARCHAR | NOT NULL | S3 URL |
| status | VARCHAR | NOT NULL | PENDING, APPROVED, REJECTED |
| admin_memo | VARCHAR | | 거절 사유 |
| reviewed_by | BIGINT | FK → Member | 관리자 ID |
| reviewed_at | TIMESTAMP | | |
| created_at | TIMESTAMP | NOT NULL | |

## Post (게시글)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| member_id | BIGINT | FK → Member | |
| title | VARCHAR | NOT NULL | |
| content | TEXT | NOT NULL | TipTap JSON |
| therapy_area | VARCHAR | | 작업, 언어, 인지, 놀이, 기타 |
| is_anonymous | BOOLEAN | NOT NULL | |
| view_count | INT | DEFAULT 0 | Redis 배치 반영 |
| like_count | INT | DEFAULT 0 | Redis 배치 반영 |
| comment_count | INT | DEFAULT 0 | 역정규화 |
| status | VARCHAR | NOT NULL | ACTIVE, HIDDEN, DELETED |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | | soft delete |

## Comment (댓글/대댓글)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| post_id | BIGINT | FK → Post | |
| member_id | BIGINT | FK → Member | |
| parent_id | BIGINT | FK → Comment | 대댓글, nullable |
| content | TEXT | NOT NULL | |
| is_anonymous | BOOLEAN | NOT NULL | |
| like_count | INT | DEFAULT 0 | |
| status | VARCHAR | NOT NULL | ACTIVE, DELETED |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | | soft delete |

## File (파일)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| member_id | BIGINT | FK → Member | |
| post_id | BIGINT | FK → Post | nullable |
| original_name | VARCHAR | NOT NULL | |
| s3_url | VARCHAR | NOT NULL | |
| file_size | BIGINT | NOT NULL | |
| mime_type | VARCHAR | NOT NULL | |
| created_at | TIMESTAMP | NOT NULL | |
| deleted_at | TIMESTAMP | | soft delete |

## Like (좋아요)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| member_id | BIGINT | FK → Member | |
| target_type | VARCHAR | NOT NULL | POST, COMMENT |
| target_id | BIGINT | NOT NULL | |
| created_at | TIMESTAMP | NOT NULL | |
| | | UNIQUE (member_id, target_type, target_id) | |

## Scrap (스크랩)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| member_id | BIGINT | FK → Member | |
| post_id | BIGINT | FK → Post | |
| created_at | TIMESTAMP | NOT NULL | |
| | | UNIQUE (member_id, post_id) | |

## Report (신고)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| reporter_id | BIGINT | FK → Member | |
| target_type | VARCHAR | NOT NULL | POST, COMMENT |
| target_id | BIGINT | NOT NULL | |
| reason | TEXT | NOT NULL | |
| status | VARCHAR | NOT NULL | PENDING, PROCESSED |
| created_at | TIMESTAMP | NOT NULL | |

## Block (차단)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| blocker_id | BIGINT | FK → Member | 차단한 사람 |
| blocked_id | BIGINT | FK → Member | 차단당한 사람 |
| created_at | TIMESTAMP | NOT NULL | |
| | | UNIQUE (blocker_id, blocked_id) | |

## Notification (알림)
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| receiver_id | BIGINT | FK → Member | 받는 사람 |
| sender_id | BIGINT | FK → Member | 보내는 사람 nullable |
| type | VARCHAR | NOT NULL | COMMENT_ON_POST, REPLY_ON_COMMENT, LIKE_ON_POST, LIKE_ON_COMMENT |
| target_type | VARCHAR | NOT NULL | POST, COMMENT |
| target_id | BIGINT | NOT NULL | |
| is_read | BOOLEAN | DEFAULT FALSE | |
| created_at | TIMESTAMP | NOT NULL | |

---

## 테이블 관계
```
Member      1:N    Post
Member      1:N    Comment
Member      1:1    License
Member      1:N    File
Member      1:N    Notification (receiver)
Member      1:N    Notification (sender)
Post        1:N    Comment
Post        1:N    File
Post        1:N    Like
Post        1:N    Report
Post        1:N    Scrap
Comment     1:N    Comment (대댓글)
Comment     1:N    Like
Comment     1:N    Report
Member      N:M    Member (Block)
```

---

## Redis 키 설계

| 키 | 타입 | TTL | 설명 |
|---|---|---|---|
| refresh:{memberId} | String | 30일 | RefreshToken |
| like:post:{postId} | String | - | 게시글 좋아요 카운터 |
| like:comment:{commentId} | String | - | 댓글 좋아요 카운터 |
| view:post:{postId} | String | - | 게시글 조회수 카운터 |

---

## 배치 처리

| 작업 | 주기 | 설명 |
|---|---|---|
| 조회수 동기화 | 5분마다 | Redis view:post → PostgreSQL Post.view_count |
| 좋아요 동기화 | 1분마다 | Redis like:* → PostgreSQL like_count |
| 파일 삭제 | 매일 새벽 3시 | deleted_at IS NOT NULL 파일 S3에서 실제 삭제 |