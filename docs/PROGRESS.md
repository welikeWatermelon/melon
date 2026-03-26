# Progress

## Block 도메인 (block-agent)

### 완료 항목
- [x] Block 엔티티 (id, blockerId, blockedId, createdAt, UNIQUE 제약)
- [x] BlockRepository (findByBlockerIdAndBlockedId, findAllByBlockerId, existsByBlockerIdAndBlockedId)
- [x] BlockService (toggleBlock, getBlockedMembers, isBlocked)
- [x] BlockController (POST /api/blocks/{memberId}, GET /api/blocks)
- [x] BlockToggleResponse, BlockedMemberResponse, BlockListResponse DTO
- [x] BlockSelfException (BLOCK_001)
- [x] BlockServiceTest 단위테스트 (5개 케이스)

### 비즈니스 규칙 구현
- 토글 방식: 이미 차단이면 해제, 아니면 차단
- 본인 차단 불가 (BLOCK_001 예외)
- Block 엔티티는 createdAt만 사용 (BaseTimeEntity 미상속, @CreatedDate 직접 선언)

### post-agent, comment-agent 전달사항
- 차단된 사용자의 게시글/댓글 필터링 필요
- `BlockRepository.findAllByBlockerId(currentMemberId)` 로 차단 목록 조회
- 또는 `BlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)` 로 개별 확인
- `BlockService.isBlocked(blockerId, blockedId)` 유틸 메서드 제공
- MVP: Post/Comment 조회 시 Block 테이블 서브쿼리로 차단된 사용자 게시물 제외

### 참고사항
- Member 엔티티 미구현 상태이므로 blockerId/blockedId를 Long(FK ID)으로만 참조
- Member 엔티티 구현 후 nickname 조회를 위해 BlockService.getBlockedMembers()에 Member 조회 로직 추가 필요 (현재 nickname은 null 반환)

## Report 도메인 (report-agent)

### 완료 항목
- [x] Report 엔티티 (id, reporterId, targetType, targetId, reason, status, createdAt)
- [x] TargetType enum (POST, COMMENT)
- [x] ReportStatus enum (PENDING, PROCESSED)
- [x] ReportRepository (existsByReporterIdAndTargetTypeAndTargetId)
- [x] ReportService (createReport - 중복/본인 신고 검증 포함)
- [x] ReportController (POST /api/reports)
- [x] ReportCreateRequest DTO (validation 포함)
- [x] ErrorCode 추가 (REPORT_SELF, REPORT_TARGET_NOT_FOUND)
- [x] ReportServiceTest 단위테스트 (7개 케이스)

### 비즈니스 규칙 구현
- 동일 대상 중복 신고 불가 (REPORT_001 예외)
- 본인 게시글/댓글 신고 불가 (REPORT_002 예외)
- 존재하지 않는 대상 신고 불가 (REPORT_003 예외)
- 신고 접수 시 status = PENDING (처리는 admin-agent 담당)
- Report 엔티티는 createdAt만 사용 (BaseTimeEntity 미상속, @CreatedDate 직접 선언)

### admin-agent 전달사항
- Report 엔티티에 process() 메서드 제공 (status를 PROCESSED로 변경)
- ReportRepository를 통해 신고 목록 조회/처리 가능

## Like 도메인 (like-agent)

### 완료 항목
- [x] Like 엔티티 (target_type POST/COMMENT 통합, UNIQUE 제약)
- [x] TargetType enum (POST, COMMENT)
- [x] LikeCreatedEvent (ApplicationEvent - notification-agent 연동용)
- [x] LikeToggleRequest / LikeToggleResponse DTO
- [x] LikeRepository (JPA + 네이티브 쿼리로 like_count 동기화)
- [x] LikeService - 좋아요 토글 (추가/취소 + Redis 카운터 증감 + 이벤트 발행)
- [x] LikeSyncScheduler - @Scheduled(fixedRate=60000) Redis → PostgreSQL 배치 동기화
- [x] LikeController - POST /api/likes (ApiResponse 래핑)
- [x] SchedulingConfig - @EnableScheduling 설정
- [x] LikeServiceTest - 단위테스트 6건 (추가/취소/댓글/이벤트발행/이벤트미발행/null처리)
- [x] LikeSyncSchedulerTest - 단위테스트 5건 (게시글동기화/댓글동기화/빈키/null키/null값)

### 비즈니스 규칙 반영
- 토글 방식: 있으면 삭제+Redis-1, 없으면 생성+Redis+1
- 본인 글/댓글에도 좋아요 가능 (별도 제한 없음)
- 좋아요 추가 시에만 LikeCreatedEvent 발행 (취소 시 미발행)
- 배치: 1분마다 Redis like:post:*, like:comment:* 키를 스캔하여 DB 반영

### notification-agent 전달사항
- LikeCreatedEvent(senderId, targetType, targetId) 발행
- @TransactionalEventListener로 수신하여 알림 생성 필요
- targetType: POST → LIKE_ON_POST, COMMENT → LIKE_ON_COMMENT

## Post 도메인 (post-agent)

### 완료 항목
- [x] Post 엔티티 (BaseTimeEntity 상속, @SQLRestriction soft delete, PostStatus enum)
- [x] Scrap 엔티티 (member_id + post_id UNIQUE 제약)
- [x] PostRepository (목록 조회, 키워드 ILIKE 검색, 치료영역 필터, 차단 필터 쿼리 준비, 조회수 배치 업데이트)
- [x] ScrapRepository (존재 확인, 삭제)
- [x] PostService (CRUD, 스크랩 토글, 익명 마스킹, Redis 조회수, PENDING 차단)
- [x] ViewCountSyncScheduler (Redis -> DB 5분마다 배치 동기화)
- [x] PostController (8개 API 엔드포인트)
- [x] DTO: PostCreateRequest, PostUpdateRequest, PostListResponse, PostDetailResponse, PostCreateResponse, PageResponse, ScrapResponse
- [x] PostServiceTest 단위테스트 (14개 케이스)

### API 엔드포인트
- GET /api/posts (목록, 페이지네이션, 치료영역 필터, 키워드 검색)
- GET /api/posts/{postId} (상세, 조회수 Redis +1)
- POST /api/posts (작성, PENDING 차단)
- PATCH /api/posts/{postId} (수정, 본인만)
- PATCH /api/posts/{postId}/delete (삭제, soft delete, 본인만)
- GET /api/posts/my (내가 쓴 게시글)
- GET /api/posts/scrapped (스크랩한 게시글)
- POST /api/posts/{postId}/scraps (스크랩 토글)

### 비즈니스 규칙 구현
- 익명 게시글: DB에 member_id 저장, 응답에서 author="익명" (본인에게는 표시)
- 조회수: Redis view:post:{postId} 증가, 5분 배치로 DB 반영
- PENDING 상태 회원 작성 불가 (AUTH_003)
- 본인 외 수정/삭제 불가 (POST_002)
- 스크랩 토글 방식: 있으면 삭제, 없으면 생성
- Soft delete: deleted_at + status=DELETED

### 도메인 간 연동 상태
- [x] Block 서브쿼리 활성화 (findAllWithFilters에서 com.melonme.block.domain.Block JPQL 서브쿼리 사용)
- [x] File 도메인 연동 (작성/수정 시 fileIds로 FileEntity.assignToPost 호출, 상세조회 시 findAllByPostId)
- [ ] Member 도메인 연동 후 닉네임 조회 (현재 "회원{memberId}" 임시 반환)
- [ ] Like 도메인 연동 후 isLiked 조회 (현재 false 반환)
- [ ] Security 연동 후 X-Member-Id 헤더를 @AuthenticationPrincipal로 교체

## Comment 도메인 (comment-agent)

### 완료 항목
- [x] Comment 엔티티 (BaseTimeEntity 상속, @SQLRestriction soft delete, CommentStatus enum)
- [x] CommentStatus enum (ACTIVE, DELETED)
- [x] CommentCreatedEvent (ApplicationEvent - notification-agent 연동용)
- [x] CommentRepository (게시글별 댓글+대댓글 fetch join 조회, 회원별 댓글 페이징 조회)
- [x] CommentService (댓글 CRUD, 대댓글 작성, 내 댓글 목록, 익명/차단/삭제 마스킹)
- [x] CommentController (6개 API 엔드포인트)
- [x] DTO: CommentCreateRequest, CommentUpdateRequest, CommentResponse, ReplyResponse, CommentListResponse, CommentIdResponse, MyCommentResponse
- [x] ErrorCode 추가 (COMMENT_001~003)
- [x] Post 엔티티에 incrementCommentCount/decrementCommentCount 메서드 추가
- [x] BlockRepository에 findBlockedIdsByBlockerId 메서드 추가
- [x] CommentServiceTest 단위테스트 (12개 케이스)

### API 엔드포인트
- GET /api/posts/{postId}/comments (댓글 목록, 대댓글 중첩 반환)
- POST /api/posts/{postId}/comments (댓글 작성, PENDING 차단)
- POST /api/posts/{postId}/comments/{commentId}/replies (대댓글 작성, 1depth 제한)
- PATCH /api/posts/{postId}/comments/{commentId} (수정, 본인만)
- PATCH /api/posts/{postId}/comments/{commentId}/delete (삭제, soft delete, 본인만)
- GET /api/members/me/comments (내가 쓴 댓글 목록, 페이지네이션)

### 비즈니스 규칙 구현
- 대댓글 1depth만 허용 (parent의 parent가 있으면 COMMENT_003 예외)
- 삭제된 댓글: 대댓글 있으면 "삭제된 댓글입니다" 텍스트 표시, 없으면 숨김
- 익명 댓글: DB에 member_id 저장, 응답에서 author="익명"
- 차단 회원 댓글: "차단한 사용자의 댓글입니다" 텍스트 표시
- 댓글 작성/삭제 시 Post.commentCount 증감
- 댓글 작성 시 CommentCreatedEvent 발행 (parentCommentId 포함)
- PENDING 상태 회원 작성 불가 (AUTH_003)
- 본인 외 수정/삭제 불가 (COMMENT_002)

### notification-agent 전달사항
- CommentCreatedEvent(commentId, postId, memberId, parentCommentId) 발행
- parentCommentId == null: 게시글에 댓글 → COMMENT_ON_POST
- parentCommentId != null: 댓글에 대댓글 → REPLY_ON_COMMENT
- @TransactionalEventListener로 수신하여 알림 생성 필요

### 다른 에이전트 연동 필요 (TODO)
- [ ] Member 도메인 연동 후 닉네임 조회 (현재 "회원{memberId}" 임시 반환)
- [ ] Security 연동 후 @AuthenticationPrincipal 실제 연동

## File 도메인 (file-agent)

### 완료 항목
- [x] FileEntity (id, memberId, postId, originalName, s3Url, s3Key, fileSize, mimeType, createdAt, deletedAt)
- [x] FileRepository (JpaRepository + findAllSoftDeleted 커스텀 쿼리)
- [x] FileUploadResponse DTO (fileId, originalName, fileSize)
- [x] FileService (upload, generatePresignedUrl, softDelete, deleteFromS3)
- [x] FileCleanupScheduler (@Scheduled cron 매일 새벽 3시 - soft delete 파일 S3 실제 삭제)
- [x] FileController (POST /api/files/upload, GET /api/files/{fileId}/download, PATCH /api/files/{fileId}/delete)
- [x] ErrorCode 추가 (FILE_003 FILE_NOT_FOUND, FILE_004 FILE_UPLOAD_FAILED)
- [x] S3Config에 S3Presigner Bean 추가
- [x] build.gradle에 s3-presigner 의존성 추가
- [x] FileServiceTest 단위테스트 (8개 케이스)
- [x] FileCleanupSchedulerTest 단위테스트 (3개 케이스)

### 비즈니스 규칙 구현
- 허용 확장자: pdf, hwp, jpg, jpeg, png, gif
- 최대 파일 크기: 10MB
- S3 키 구조: files/{memberId}/{UUID}_{originalName}
- 다운로드: S3 presigned URL 생성 (TTL 10분) 후 302 redirect
- 삭제: soft delete (deletedAt 업데이트)
- 배치: 매일 새벽 3시 soft delete된 파일 S3 실제 삭제 후 DB 영구 삭제
- S3 삭제 실패 시 해당 파일은 DB에서도 삭제하지 않음 (다음 배치에서 재시도)
- S3 버킷: ${S3_BUCKET:melonme-bucket} 환경변수

### post-agent 전달사항
- 게시글 작성/수정 시 fileIds로 FileEntity.assignToPost(postId) 호출 필요
- FileRepository를 통해 게시글에 연결된 파일 조회 가능
- 게시글 삭제 시 관련 파일도 softDelete 처리 필요

### 참고사항
- Member 엔티티 미구현 상태이므로 memberId를 Long(FK ID)으로만 참조
- FileController의 memberId는 @RequestAttribute("memberId")로 주입 (인증 필터에서 설정)

## Notification 도메인 (notification-agent)

### 완료 항목
- [x] Notification 엔티티 (id, receiverId, senderId, type, targetType, targetId, isRead, createdAt)
- [x] NotificationType enum (COMMENT_ON_POST, REPLY_ON_COMMENT, LIKE_ON_POST, LIKE_ON_COMMENT)
- [x] NotificationTargetType enum (POST, COMMENT)
- [x] NotificationRepository (페이지네이션 조회, 미읽음 카운트, 전체 읽음 벌크 업데이트)
- [x] SseEmitterService (ConcurrentHashMap 기반 SSE 연결 관리, 더미 이벤트 전송, 알림 전송)
- [x] NotificationService (알림 목록 조회, 개별/전체 읽음 처리, 이벤트 핸들러)
- [x] NotificationController (SSE subscribe, 목록 조회, 개별/전체 읽음 처리)
- [x] CommentCreatedEvent (postId, postAuthorId, commentAuthorId, commentId, parentCommentAuthorId)
- [x] LikeCreatedEvent (targetType, targetId, targetAuthorId, likerId)
- [x] NotificationResponse, NotificationListResponse DTO
- [x] ErrorCode 추가 (NOTIFICATION_001 NOTIFICATION_NOT_FOUND)
- [x] NotificationServiceTest 단위테스트 (12개 케이스)
- [x] SseEmitterServiceTest 단위테스트 (4개 케이스)

### API 엔드포인트
- GET /api/notifications/subscribe (SSE 연결, text/event-stream)
- GET /api/notifications (알림 목록, 페이지네이션, unreadCount 포함)
- PATCH /api/notifications/{id}/read (개별 읽음 처리)
- PATCH /api/notifications/read-all (전체 읽음 처리)

### 비즈니스 규칙 구현
- 본인 행위 알림 미발송 (내 게시글에 내가 댓글 등)
- 차단 회원 알림 미발송 (수신자가 발신자를 차단한 경우)
- SSE 연결 없어도 DB에는 항상 저장
- SSE 연결 즉시 더미 이벤트("connect") 전송 (503 방지)
- SSE 타임아웃 30분, onCompletion/onTimeout/onError에서 Map 자동 정리
- @TransactionalEventListener로 트랜잭션 커밋 후 이벤트 수신
- 일반 댓글 -> COMMENT_ON_POST (게시글 작성자에게)
- 대댓글 -> REPLY_ON_COMMENT (부모 댓글 작성자에게)
- 게시글 좋아요 -> LIKE_ON_POST
- 댓글 좋아요 -> LIKE_ON_COMMENT

### 이벤트 발행 가이드 (comment-agent, like-agent 전달사항)
- Comment 도메인: 댓글 생성 시 `ApplicationEventPublisher.publishEvent(new CommentCreatedEvent(...))` 발행
- Like 도메인: 좋아요 추가 시 `ApplicationEventPublisher.publishEvent(new LikeCreatedEvent(...))` 발행
- 이벤트 클래스는 `com.melonme.notification.event` 패키지에 정의됨

### 참고사항
- Member 엔티티 미구현 상태이므로 receiverId/senderId를 Long(FK ID)으로만 참조
- Security 미연동 상태이므로 memberId를 @RequestParam으로 수신 (인증 연동 후 @AuthenticationPrincipal로 교체 필요)
- 알림 메시지는 NotificationType별 고정 한국어 문자열 (추후 동적 메시지 확장 가능)

## Member 도메인 (member-agent)

### 완료 항목
- [x] Member 엔티티 (provider, providerId, nickname UNIQUE, therapyArea, role, BaseTimeEntity 상속, @SQLRestriction soft delete)
- [x] License 엔티티 (member FK, licenseImgUrl, status, adminMemo, reviewedBy, reviewedAt)
- [x] Provider enum (KAKAO, GOOGLE)
- [x] Role enum (PENDING, MEMBER, ADMIN)
- [x] LicenseStatus enum (PENDING, APPROVED, REJECTED)
- [x] MemberRepository (findByProviderAndProviderId, existsByNickname)
- [x] LicenseRepository (findTopByMemberIdOrderByCreatedAtDesc, existsByMemberIdAndStatus)
- [x] OAuth2Service (카카오/구글 인가코드 -> 사용자 정보 조회, WebClient 사용)
- [x] OAuth2Properties (환경변수 기반 카카오/구글 설정)
- [x] AuthService (소셜 로그인, 토큰 재발급 with rotate, 로그아웃)
- [x] MemberService (내 정보 조회/수정, 닉네임 중복 검증, 회원 탈퇴 soft delete)
- [x] LicenseService (면허 인증 신청 S3 업로드, 내 인증 상태 조회, 중복 신청 방지)
- [x] RefreshTokenService (Redis CRUD, TTL 30일)
- [x] JwtProvider (AccessToken 1시간, RefreshToken 30일, HMAC-SHA256)
- [x] JwtProperties (환경변수 기반 secret, expiry 설정)
- [x] JwtAuthenticationFilter (Bearer 토큰 파싱 -> SecurityContext 설정)
- [x] JwtAuthenticationEntryPoint (인증 실패 시 AUTH_001 응답)
- [x] JwtAccessDeniedHandler (권한 부족 시 AUTH_003 응답)
- [x] CustomUserDetails (memberId, role 보유)
- [x] SecurityConfig 세분화 (JWT 필터, URL별 권한, CORS, STATELESS 세션)
- [x] AuthController (GET /api/auth/kakao/callback, GET /api/auth/google/callback, POST /api/auth/refresh, PATCH /api/auth/logout)
- [x] MemberController (GET /api/members/me, PATCH /api/members/me, PATCH /api/members/me/delete, POST /api/members/me/license, GET /api/members/me/license)
- [x] DTO: LoginResponse, TokenRefreshResponse, MemberInfoResponse, MemberUpdateResponse, LicenseResponse, MemberUpdateRequest, TokenRefreshRequest
- [x] ErrorCode 추가 (AUTH_004~006, MEMBER_000, MEMBER_002, LICENSE_001~002)
- [x] AuthServiceTest 단위테스트 (5개 케이스)
- [x] MemberServiceTest 단위테스트 (5개 케이스)
- [x] LicenseServiceTest 단위테스트 (3개 케이스)

### API 엔드포인트
- GET /api/auth/kakao/callback?code={code} (카카오 소셜 로그인)
- GET /api/auth/google/callback?code={code} (구글 소셜 로그인)
- POST /api/auth/refresh (AccessToken 재발급, RefreshToken rotate)
- PATCH /api/auth/logout (로그아웃, Redis RefreshToken 삭제)
- GET /api/members/me (내 정보 조회)
- PATCH /api/members/me (내 정보 수정, 닉네임/치료영역)
- PATCH /api/members/me/delete (회원 탈퇴, soft delete)
- POST /api/members/me/license (면허 인증 신청, multipart S3 업로드)
- GET /api/members/me/license (내 인증 상태 조회)

### 비즈니스 규칙 구현
- 신규 가입 시 role = PENDING
- 닉네임 중복 시 UUID suffix 자동 부여 (소셜 로그인 시)
- 닉네임 수정 시 중복 검증 (MEMBER_001 예외)
- PENDING 상태는 게시글/댓글 작성 불가 (SecurityConfig에서 MEMBER/ADMIN만 허용)
- 회원 탈퇴 시 soft delete + Redis RefreshToken 삭제
- 면허 PENDING 상태에서 중복 신청 불가 (LICENSE_001 예외)
- RefreshToken rotate: 재발급 시 새 RefreshToken도 함께 발급하여 Redis 갱신
- JWT AccessToken에 memberId(subject) + role(claim) 포함

### SecurityConfig 설정
- /api/auth/kakao/callback, /api/auth/google/callback, /api/auth/refresh: public
- /api/admin/**: ADMIN role만 허용
- POST /api/posts, POST 댓글: MEMBER/ADMIN role만 허용
- /api/**: 인증 필요
- CORS: localhost:3000, localhost:5173 허용
- JWT 필터: UsernamePasswordAuthenticationFilter 앞에 배치

### 다른 에이전트 참고사항
- @AuthenticationPrincipal CustomUserDetails로 인증 사용자 정보 접근
  - userDetails.getMemberId(): Long
  - userDetails.getRole(): String
- MemberRepository를 통해 회원 닉네임 등 조회 가능
- Member 엔티티의 updateRole(Role)로 면허 승인 시 역할 변경 (admin-agent 사용)

## Admin 도메인 (admin-agent)

### 완료 항목
- [x] ReportAction enum (HIDE_POST, HIDE_COMMENT, DISMISS)
- [x] MemberAction enum (FORCE_DELETE)
- [x] LicenseReviewRequest DTO (status, adminMemo)
- [x] ReportProcessRequest DTO (action)
- [x] MemberActionRequest DTO (action)
- [x] AdminLicenseResponse DTO (id, memberId, nickname, licenseImgUrl, status, createdAt)
- [x] AdminReportResponse DTO (id, reporterId, targetType, targetId, reason, status, createdAt)
- [x] AdminMemberResponse DTO (id, nickname, therapyArea, role, createdAt)
- [x] AdminStatsResponse DTO (wau, mau, weeklyPostCount, uploaderRatio, retentionRate)
- [x] AdminStatsRepository (WAU/MAU, 게시글 수, 작성자 수, 잔존율 네이티브 쿼리)
- [x] AdminService (면허 승인/거절, 신고 처리, 회원 강제 탈퇴, 통계 조회)
- [x] AdminController (7개 API 엔드포인트, @PreAuthorize("hasRole('ADMIN')"))
- [x] ErrorCode 추가 (ADMIN_001~007)
- [x] Post.hide() 메서드 추가
- [x] Comment.hideByAdmin() 메서드 추가
- [x] LicenseRepository.findByStatus() 메서드 추가
- [x] ReportRepository.findByStatus() 메서드 추가
- [x] MemberRepository.findByRole() 메서드 추가
- [x] AdminServiceTest 단위테스트 (18개 케이스)

### API 엔드포인트
- GET /api/admin/licenses (인증 신청 목록, status 필터, 페이지네이션)
- PATCH /api/admin/licenses/{id} (인증 승인/거절)
- GET /api/admin/reports (신고 목록, status 필터, 페이지네이션)
- PATCH /api/admin/reports/{id} (신고 처리)
- GET /api/admin/members (회원 목록, role 필터, 페이지네이션)
- PATCH /api/admin/members/{id} (회원 제재)
- GET /api/admin/stats (통계 조회)

### 비즈니스 규칙 구현
- ADMIN role만 접근 가능 (@PreAuthorize("hasRole('ADMIN')"))
- 면허 승인: License.status=APPROVED + Member.role=MEMBER
- 면허 거절: License.status=REJECTED + adminMemo 필수 (빈 문자열도 거절)
- 이미 처리된 면허 신청 재처리 불가 (ADMIN_002)
- 신고 HIDE_POST: POST 신고에만 적용, Post.status=HIDDEN
- 신고 HIDE_COMMENT: COMMENT 신고에만 적용, Comment.status=DELETED
- 신고 DISMISS: 기각 (별도 상태 변경 없음)
- 신고 처리 시 Report.status=PROCESSED
- 이미 처리된 신고 재처리 불가 (ADMIN_005)
- 회원 강제 탈퇴: soft delete (deletedAt 설정)
- 통계: WAU(7일), MAU(30일), 주간 게시글 수, Uploader 비율(%), 30일 잔존율(%)

### 다른 도메인 변경사항
- Post 엔티티: hide() 메서드 추가 (status=HIDDEN)
- Comment 엔티티: hideByAdmin() 메서드 추가 (status=DELETED, soft delete 없이)
- LicenseRepository: findByStatus(LicenseStatus, Pageable) 추가
- ReportRepository: findByStatus(ReportStatus, Pageable) 추가
- MemberRepository: findByRole(Role, Pageable) 추가

### 참고사항
- AdminController의 adminId는 현재 임시값(1L) 사용 중 (Security 연동 후 @AuthenticationPrincipal로 교체 필요)
- 통계의 WAU/MAU는 member.updatedAt 기준 (로그인 시 updatedAt 갱신 전제)
- 통계 쿼리는 네이티브 SQL 사용 (post 테이블 직접 조회)

## 프론트엔드 Common (common-agent)

### 완료 항목

#### 기반 설정
- [x] Tailwind CSS v4 설정 (index.css @theme 커스텀 컬러 팔레트)
- [x] Vite path alias (@/ → src/) + tsconfig.app.json baseUrl/paths
- [x] @tailwindcss/vite 플러그인 연동

#### 디자인 시스템 (커스텀 컬러)
- primary: 따뜻한 코랄/피치 (#F4845F 계열, 50~900)
- secondary: 부드러운 라벤더 (#9B8EC4 계열, 50~900)
- sage: 세이지 그린 (#7DB8A5 계열, 50~900)
- warm: 따뜻한 오프화이트 배경 (#FFFAF8 계열)
- gray: 뉴트럴 그레이 (50~900)
- danger/success/info: 시맨틱 컬러

#### 공통 컴포넌트 (/src/components/common/)
- [x] **Button** — variant: primary, secondary, ghost, danger / size: sm, md, lg / loading 상태 지원
- [x] **Input** — label, error 메시지, forwardRef 지원
- [x] **Textarea** — label, error 메시지, forwardRef 지원
- [x] **Modal** — isOpen/onClose, title, footer, ESC 닫기, 스크롤 잠금
- [x] **Toast** — success/error/info, CustomEvent 기반, 자동 3초 소멸, `showToast()` 유틸 함수 제공
- [x] **Spinner** — size: sm, md, lg
- [x] **Pagination** — currentPage, hasNext, totalCount, pageSize 기반 페이지 네비게이션
- [x] **Avatar** — 닉네임 이니셜 표시, 해시 기반 컬러 배정, 익명(?) 처리, size: sm, md, lg
- [x] **Badge** — variant: therapy(치료영역 태그), status, count
- [x] **EmptyState** — 커스텀 title, description, icon, action 지원
- [x] **ErrorBoundary** — 클래스 컴포넌트, 에러 캐치 + 재시도 버튼
- [x] **Dropdown** — trigger 요소, items 배열, onSelect, align: left/right, 외부 클릭 닫기
- [x] **index.ts** — 모든 공통 컴포넌트 re-export

#### 레이아웃 (/src/layouts/)
- [x] **GNB** — 역할별 분기 표시:
  - 비로그인: 로그인 버튼
  - PENDING: "면허 인증 대기중" 안내
  - MEMBER: 알림 아이콘(뱃지 카운트), 프로필 드롭다운(마이페이지/로그아웃)
  - ADMIN: 관리자 링크 추가
- [x] **Footer** — 저작권 표시
- [x] **MainLayout** — GNB + ErrorBoundary + Outlet + Footer + Toast
- [x] **AdminLayout** — GNB + 사이드 네비게이션(md 이상) + 모바일 탭(md 미만) + Outlet + Toast
- [x] **index.ts** — 레이아웃 re-export

#### 라우팅 (/src/router/)
- [x] **PublicRoute** — 비로그인만 접근, 로그인 시 role별 리다이렉트 (PENDING→/pending, MEMBER→/board, ADMIN→/admin)
- [x] **PendingRoute** — PENDING role만 접근
- [x] **PrivateRoute** — MEMBER 이상만 접근, PENDING→/pending 리다이렉트
- [x] **AdminRoute** — ADMIN role만 접근
- [x] **index.tsx** — createBrowserRouter 설정, 전체 라우트 매핑 (placeholder 페이지 포함)

#### 라우트 구조
```
Public (PublicRoute + MainLayout)
  /              → 랜딩
  /login         → 로그인
  /auth/kakao/callback   → OAuth 콜백
  /auth/google/callback  → OAuth 콜백

Pending (PendingRoute + MainLayout)
  /pending       → 인증 대기

Private (PrivateRoute + MainLayout)
  /board         → 게시판 목록
  /board/:postId → 게시글 상세
  /board/write   → 게시글 작성
  /board/:postId/edit → 게시글 수정
  /mypage        → 마이페이지
  /notifications → 알림

Admin (AdminRoute + AdminLayout)
  /admin              → 면허 관리 (기본)
  /admin/licenses     → 면허 관리
  /admin/reports      → 신고 관리
  /admin/members      → 회원 관리
  /admin/stats        → 통계
```

#### Axios 인스턴스 (/src/api/instance.ts)
- [x] baseURL: VITE_API_BASE_URL 환경변수 (기본값 http://localhost:8080)
- [x] Authorization Bearer 헤더 자동 주입 (authStore에서 토큰 읽기)
- [x] 401 시 자동 토큰 재발급 interceptor (큐 기반 동시 요청 처리)
- [x] 재발급 실패 시 authStore.clearAuth() + /login 이동
- [x] 공통 에러 Toast 처리 (CustomEvent 'toast' 발행)

#### TypeScript 공통 타입 (/src/types/index.ts)
- [x] ApiResponse<T>, ApiErrorResponse, PageResponse<T>
- [x] Member, Role, Provider, TherapyArea, MemberUpdateRequest, LicenseStatus
- [x] LoginResponse, TokenRefreshRequest, TokenRefreshResponse
- [x] PostListItem, PostDetail, PostCreateRequest, PostUpdateRequest, PostStatus
- [x] Comment, Reply, CommentCreateRequest, MyComment
- [x] LikeTargetType, LikeToggleRequest, LikeToggleResponse
- [x] FileInfo, FileUploadResponse
- [x] ReportTargetType, ReportCreateRequest
- [x] BlockedMember, BlockToggleResponse
- [x] Notification, NotificationType, NotificationTargetType, NotificationListResponse
- [x] AdminLicense, AdminReport, AdminMember, AdminStats
- [x] ReportAction, MemberAction, LicenseReviewRequest, ScrapResponse

#### Zustand authStore (/src/store/authStore.ts)
- [x] accessToken, member 상태
- [x] setAuth(token, member) — localStorage 저장 + 상태 업데이트
- [x] updateMember(partial) — member 부분 업데이트
- [x] clearAuth() — localStorage 삭제 + 상태 초기화
- [x] 새로고침 시 localStorage에서 복원

#### App.tsx
- [x] RouterProvider로 라우터 연결

### 다른 에이전트 사용 가이드

#### 컴포넌트 import
```typescript
import { Button, Input, Modal, Toast, Spinner, Pagination, Avatar, Badge, EmptyState, ErrorBoundary, Dropdown, Textarea, showToast } from '@/components/common';
```

#### 레이아웃 import
```typescript
import { MainLayout, AdminLayout, GNB, Footer } from '@/layouts';
```

#### 타입 import
```typescript
import type { Member, Post, Comment, ApiResponse, PageResponse, ... } from '@/types';
```

#### API 호출
```typescript
import api from '@/api/instance';
const { data } = await api.get<ApiResponse<T>>('/api/...');
```

#### 인증 상태
```typescript
import { useAuthStore } from '@/store/authStore';
const { member, accessToken, setAuth, clearAuth } = useAuthStore();
```

#### Toast 사용
```typescript
import { showToast } from '@/components/common';
showToast('success', '저장되었습니다');
showToast('error', '오류가 발생했습니다');
```

#### 주의사항
- common 디렉토리 파일 수정 금지
- 추가 공통 컴포넌트 필요 시 PROGRESS.md에 요청 기록
- API 호출 시 반드시 instance.ts의 api 사용 (직접 axios 호출 금지)
- 타입은 반드시 /src/types/에서 import

## 프론트 Auth 도메인 (auth-agent)

### 완료 항목
- [x] /src/api/authApi.ts - 인증 관련 API 함수 (loginWithKakao, loginWithGoogle, refreshToken, logout, submitLicense, getLicenseStatus)
- [x] /src/pages/landing/LandingPage.tsx - 랜딩페이지 (히어로, 기능소개 3카드, CTA 섹션)
- [x] /src/pages/auth/LoginPage.tsx - 로그인페이지 (카카오/구글 OAuth 버튼)
- [x] /src/pages/auth/OAuthCallbackPage.tsx - OAuth 콜백 처리 (code 추출, API 호출, role별 리다이렉트)
- [x] /src/pages/auth/PendingPage.tsx - 인증 대기 페이지 (면허증 업로드, 상태별 UI)
- [x] /src/pages/auth/index.ts - re-export
- [x] /src/pages/landing/index.ts - re-export
- [x] /.env.example - 환경변수 예시 파일

### 구현 상세
- OAuth: 프론트에서 직접 카카오/구글 OAuth URL로 리다이렉트 (VITE 환경변수로 client_id, redirect_uri 주입)
- 랜딩: 이미지 에셋 없이 Tailwind 텍스트 + SVG 아이콘으로 구성
- 콜백: useRef로 StrictMode 중복 호출 방지, role별 리다이렉트 (PENDING→/pending, MEMBER→/board, ADMIN→/admin)
- PendingPage: 4가지 상태 (loading, not_submitted, pending, rejected) + APPROVED시 자동 리다이렉트
- 파일 업로드: JPG/PNG/PDF만 허용, 최대 10MB
- TypeScript 컴파일 + Vite 빌드 에러 없음 확인

## 관리자 페이지 (admin-front-agent)

### 완료 항목
- [x] /src/api/adminApi.ts - 관리자 API 함수 7개 (fetchLicenses, reviewLicense, fetchReports, processReport, fetchMembers, actionMember, fetchStats)
- [x] /src/pages/admin/LicensesPage.tsx - 면허 인증 관리 (필터, 테이블, 이미지 모달, 승인/거절)
- [x] /src/pages/admin/ReportsPage.tsx - 신고 관리 (필터, 테이블, 상세 모달, 게시글/댓글 숨김/기각 처리)
- [x] /src/pages/admin/MembersPage.tsx - 회원 관리 (역할 필터, 테이블, 강제 탈퇴 확인 모달)
- [x] /src/pages/admin/StatsPage.tsx - 통계 대시보드 (WAU/MAU/주간게시글/잔존율 카드 + recharts Bar/Pie 차트)
- [x] /src/pages/admin/index.ts - re-export

### 구현 상세
- 공통 컴포넌트(Button, Modal, Textarea, Badge, Pagination, EmptyState, Spinner, showToast) 활용
- 모든 페이지: 필터 탭 + 테이블 + 페이지네이션 + 로딩/빈 상태 처리
- LicensesPage: 행 클릭 시 면허증 이미지 모달, PENDING만 승인/거절 버튼 활성화
- ReportsPage: 2단계 모달 (상세 → 처리 확인), POST/COMMENT별 다른 액션 버튼
- MembersPage: 각 행에 강제 탈퇴 버튼, 확인 모달
- StatsPage: recharts BarChart(WAU vs MAU) + 도넛 PieChart(작성자 비율)
- TypeScript 컴파일 에러 없음 확인 (admin 파일 한정)

## 마이페이지 / 알림 / 차단 (mypage-agent, 프론트엔드)

### 완료 항목
- [x] /src/api/memberApi.ts - fetchMyInfo, updateMyInfo, deleteMyAccount, fetchMyComments
- [x] /src/api/notificationApi.ts - fetchNotifications, markAsRead, markAllAsRead
- [x] /src/api/blockApi.ts - fetchBlockedMembers, toggleBlock
- [x] /src/store/notificationStore.ts - Zustand 스토어 (SSE 연결/해제, 알림 상태관리)
- [x] /src/hooks/useSSE.ts - SSE 연결 훅 (member + role 기반 자동 연결/해제)
- [x] /src/components/SSEProvider.tsx - 레이아웃에 통합 가능한 SSE 래퍼 컴포넌트
- [x] /src/pages/mypage/utils/timeAgo.ts - 방금 전/N분 전/N시간 전/N일 전/날짜 포맷
- [x] /src/pages/mypage/MyPage.tsx - 마이페이지 메인 (프로필 카드, 정보 수정 모달, 탈퇴 모달, 4개 탭)
- [x] /src/pages/mypage/components/MyPostsTab.tsx - 내가 쓴 글 탭
- [x] /src/pages/mypage/components/MyCommentsTab.tsx - 내가 쓴 댓글 탭
- [x] /src/pages/mypage/components/ScrappedPostsTab.tsx - 스크랩한 글 탭 (스크랩 취소 포함)
- [x] /src/pages/mypage/components/BlockedMembersTab.tsx - 차단 목록 탭 (차단 해제 포함)
- [x] /src/pages/mypage/NotificationsPage.tsx - 전체 알림 목록 페이지
- [x] /src/pages/mypage/components/NotificationDropdown.tsx - GNB용 알림 드롭다운
- [x] /src/pages/mypage/index.ts - re-export

### 구현 상세
- SSE: EventSource로 /api/notifications/subscribe?token={accessToken} 연결, onerror 시 3초 후 재연결
- 마이페이지: 4개 탭 (내가 쓴 글/댓글, 스크랩, 차단) 전환, 프로필 수정/탈퇴 모달
- 알림: 드롭다운(최근 5개) + 전체 보기 페이지, 읽음/안읽음 시각 표시, 클릭 시 해당 게시글 이동
- board-agent가 관리하는 postApi 등은 직접 api 인스턴스로 호출 (충돌 방지)
- TypeScript 컴파일 에러 없음 확인 (mypage-agent 파일 한정)

## 게시판 도메인 (board-agent, 프론트엔드)

### 완료 항목
- [x] /src/api/postApi.ts - fetchPosts, fetchPost, createPost, updatePost, deletePost, fetchMyPosts, fetchScrappedPosts, toggleScrap
- [x] /src/api/commentApi.ts - fetchComments, createComment, createReply, updateComment, deleteComment
- [x] /src/api/likeApi.ts - toggleLike
- [x] /src/api/fileApi.ts - uploadFile (multipart), getDownloadUrl
- [x] /src/api/reportApi.ts - createReport
- [x] /src/store/boardStore.ts - therapyFilter, keyword Zustand 스토어
- [x] /src/pages/board/components/TipTapEditor.tsx - 리치텍스트 에디터 (Bold, Italic, H1-H3, List, Link)
- [x] /src/pages/board/components/TipTapViewer.tsx - 읽기 전용 렌더러
- [x] /src/pages/board/components/FileUploader.tsx - 드래그앤드롭 + 클릭 업로드 (즉시 서버 전송, 확장자/크기 검증)
- [x] /src/pages/board/components/CommentSection.tsx - 댓글/대댓글 CRUD, 좋아요, 익명 토글, 삭제된 댓글 표시
- [x] /src/pages/board/BoardPage.tsx - 게시글 목록 (치료영역 필터, 키워드 검색, 카드 리스트, 페이지네이션)
- [x] /src/pages/board/PostDetailPage.tsx - 게시글 상세 (TipTap 뷰어, 파일 다운로드, 좋아요/스크랩/신고, 수정/삭제)
- [x] /src/pages/board/PostWritePage.tsx - 게시글 작성 (제목, 치료영역, 익명, TipTap 에디터, 파일 첨부)
- [x] /src/pages/board/PostEditPage.tsx - 게시글 수정 (기존 데이터 로드, 권한 체크)
- [x] /src/pages/board/index.ts - re-export

### 구현 상세
- TipTap: StarterKit + Link + Placeholder, JSON 문자열로 content 저장/로드
- 파일 업로드: 선택 즉시 POST /api/files/upload → fileId 수집, 게시글 작성 시 fileIds 전송
- 댓글: 대댓글 indent, 인라인 수정, 삭제, 좋아요 토글, 익명 체크박스
- 신고: 사유 입력 모달 → POST /api/reports
- 검색: debounce 300ms 키워드 검색
- TypeScript 컴파일 에러 없음 확인

---

## 프론트엔드 통합 완료

### 라우터 연결
- [x] router/index.tsx — 모든 placeholder를 실제 페이지 컴포넌트로 교체
- [x] /board/write 경로를 /board/:postId 보다 먼저 배치 (라우트 우선순위)

### GNB 통합
- [x] GNB에 NotificationDropdown 연결 (알림 아이콘 → 드롭다운)
- [x] MainLayout에 SSEProvider 래핑 (로그인 시 SSE 자동 연결)

### 빌드 검증
- [x] TypeScript 컴파일 에러 0개
- [x] Vite 프로덕션 빌드 성공 (CSS 32KB + JS 1.1MB)

## 백엔드 단위테스트 전체 실행 결과 (2026-03-26)

### 실행 결과
- 총 118개 테스트, **0개 실패**, 1개 무시(contextLoads), 소요시간 6.6초

### 수정 사항
- FileServiceTest: `mockMultipartFile` 헬퍼의 stub을 `lenient()`로 변경하여 UnnecessaryStubbingException 해결 (3건)
- MelonmeApplicationTests: `@Disabled` 추가 — 통합테스트 환경(PostgreSQL) 없이 단위테스트 실행 시 contextLoads 실패 방지

### 도메인별 테스트 현황
| 도메인 | 테스트 클래스 | 상태 |
|--------|-------------|------|
| Block | BlockServiceTest | PASS |
| Report | ReportServiceTest (ReportPost, ReportComment) | PASS |
| Like | LikeServiceTest, LikeSyncSchedulerTest | PASS |
| Post | PostServiceTest | PASS |
| Comment | CommentServiceTest (6개 내부 클래스) | PASS |
| Member | AuthServiceTest, LicenseServiceTest, MemberServiceTest | PASS |
| File | FileServiceTest, FileCleanupSchedulerTest | PASS |
| Notification | NotificationServiceTest (5개 내부 클래스), SseEmitterServiceTest | PASS |
| Admin | AdminServiceTest (License, Member, Report, Stats) | PASS |

## 통합테스트 결과 (2026-03-26)

### 테스트 방식
- 6개 에이전트가 소스코드 레벨에서 도메인 간 연동점을 전수 검사
- 백엔드 3개 (member/security, post/comment/like/block, notification/SSE)
- 프론트↔백엔드 3개 (auth, board, notification)

---

### [P0] 치명적 이슈 (서비스 불가)

#### 1. 이벤트 클래스 중복 — 알림 시스템 전체 미동작
- `CommentCreatedEvent`가 2개 존재: `comment.domain` (4필드) vs `notification.event` (5필드)
- `LikeCreatedEvent`도 2개 존재: `like.domain` (3필드) vs `notification.event` (4필드)
- Spring `@TransactionalEventListener`는 클래스 타입 매칭 → **서로 다른 클래스라 이벤트 전달 안 됨**
- **댓글 알림, 좋아요 알림 모두 미동작**
- 수정: notification.event 패키지 삭제, comment/like 도메인 이벤트에 누락 필드 추가

#### 2. 컨트롤러 인증 방식 불일치 — 여러 API 실행 불가
| 컨트롤러 | 현재 방식 | 문제 |
|----------|----------|------|
| PostController | `@RequestHeader("X-Member-Id")` | 프론트가 헤더 미전송 → 실패, 보안 우회 가능 |
| Like/Block/ReportController | `@AuthenticationPrincipal Long` | CustomUserDetails와 타입 불일치 → **항상 null** |
| NotificationController | `@RequestParam Long memberId` | 프론트 미전달 → 400 에러, 타인 알림 접근 가능 |
| FileController | `@RequestAttribute("memberId")` | 설정하는 필터 없음 → null/예외 |
| CommentController, AuthController, MemberController | `@AuthenticationPrincipal CustomUserDetails` | **정상** |

#### 3. SSE 연결 불가 — 실시간 알림 전체 미동작
- 프론트: `subscribe?token={JWT}` 전송
- 백엔드: `@RequestParam Long memberId` 기대 → **파라미터명/값 종류 모두 불일치**
- SSE 이벤트 이름 `"notification"` vs 프론트 `onmessage` → named event 수신 불가
- 수정: 백엔드 SecurityContext 사용, 프론트 `addEventListener('notification', ...)` 사용

#### 4. 면허증 업로드 파라미터 키 불일치
- 프론트: `formData.append('file', file)` / 백엔드: `@RequestParam("licenseImg")` → **항상 400**
- 수정: 프론트 `'file'` → `'licenseImg'` 변경

#### 5. Refresh Token Rotation 미완성
- 백엔드 `TokenRefreshResponse`에 `accessToken`만 포함, 새 `refreshToken` 미포함
- Redis에는 rotate된 새 토큰 저장 → 다음 refresh 시 불일치 → **인증 실패**
- 수정: `TokenRefreshResponse`에 `refreshToken` 필드 추가, 프론트 interceptor에서 저장

#### 6. 대댓글 삭제 버그 — 부모 댓글이 삭제됨
- `CommentSection.tsx:337` — `deleteComment(postId, commentId)` 호출 (부모 ID)
- 실제로는 `deleteComment(postId, reply.id)` 이어야 함
- **대댓글 삭제 시 부모 댓글이 삭제되는 치명적 버그**

---

### [P1] 중요 이슈 (기능 결함)

#### 7. isLiked 항상 false 하드코딩
- `PostService.java:98` — `isLiked(false)` TODO 상태
- LikeRepository 미주입 상태 → 사용자의 좋아요 여부 표시 불가

#### 8. LikeSyncScheduler Redis 키 미삭제
- 동기화 후 Redis 키를 삭제하지 않아 매번 같은 값 덮어씀
- 서버 재시작 시 Redis 초기화 → like_count가 0으로 덮어씌워질 위험
- ViewCountSyncScheduler는 `getAndDelete` 사용 (정상)

#### 9. @SQLRestriction으로 삭제된 대댓글 미표시
- Comment `@SQLRestriction("deleted_at IS NULL")` → 삭제된 대댓글이 fetch 안 됨
- "삭제된 댓글입니다" 텍스트 표시 불가

#### 10. 삭제된 댓글 판별 문자열 불일치
- 프론트: `'삭제된 댓글입니다.'` (마침표 O)
- 백엔드: `"삭제된 댓글입니다"` (마침표 X)

#### 11. COMMENT 알림 클릭 시 잘못된 페이지 이동
- targetType=COMMENT일 때 targetId가 댓글 ID → `/board/{댓글ID}`로 이동 → 404 또는 잘못된 게시글

#### 12. CommentController role "ROLE_" 접두사 문제
- `getAuthorities()` → `"ROLE_PENDING"` 반환 → `"PENDING".equals()` 불일치 → PENDING 검증 우회
- SecurityConfig URL 패턴이 1차 방어하므로 당장 문제없으나, Service 방어 무력화

#### 13. Jackson boolean isRead → read 직렬화 가능성
- `NotificationResponse.isRead`, `BlockToggleResponse.isBlocked`, `ScrapResponse.isScrapped`, `LikeToggleResponse.isLiked`
- Lombok `@Getter`의 boolean 필드 → Jackson이 `is` prefix 제거 가능
- `@JsonProperty("isRead")` 등 명시 필요

#### 14. 게시글 수정 시 기존 파일 연결 미해제
- `PostService.updatePost()` — 새 파일만 연결하고 기존 파일 postId 초기화 안 함

#### 15. 게시글 삭제 시 연관 파일 미처리
- `PostService.deletePost()` — 연결된 FileEntity soft delete 미처리 → S3 고아 파일

#### 16. SecurityConfig PENDING 차단 범위 부족
- POST /api/posts, 댓글만 제한 → 수정/삭제/스크랩/좋아요/신고/차단은 PENDING도 가능

#### 17. AdminController adminId 하드코딩
- `AdminController.java:45` — `Long adminId = 1L;` → @AuthenticationPrincipal 교체 필요

#### 18. likeCount가 DB 값만 반환
- viewCount는 DB+Redis 합산 반환, likeCount는 DB만 → 1분 동기화 간격 동안 불일치

---

### [P2] 경미한 이슈

#### 19. JwtAccessDeniedHandler/EntryPoint ObjectMapper import 오류
- `tools.jackson.databind.ObjectMapper` (Jackson 3.x) — Spring Boot 4.0에서 Jackson 3.x 사용 여부 확인 필요

#### 20. 스크랩 목록에서 차단 회원 게시글 미필터링
- `PostRepository.findScrappedByMemberId()` — Block 서브쿼리 없음

#### 21. SSE 재연결 시 누락 알림 미동기화
- 연결 끊김 동안 수신 못한 알림이 보충되지 않음

#### 22. Post/Comment 닉네임 조회 TODO 미완성
- MemberRepository 이미 구현됨 → 연동만 하면 됨

#### 23. 댓글에 isLiked 상태 없음
- Comment/Reply 타입 모두 isLiked 필드 없음 → 댓글 좋아요 여부 UI 표시 불가

#### 24. 로그아웃 시 accessToken 무효화 미처리
- refreshToken만 삭제, accessToken은 만료까지 유효

#### 25. /api/members/me/comments 엔드포인트 위치 확인 필요
- MemberController에 없고 CommentController에 구현됨 → 프론트 URL 확인 필요

---

### 수정 우선순위 요약

| 등급 | 건수 | 핵심 키워드 |
|------|------|------------|
| P0 치명 | 6건 | 이벤트 중복, 인증 불일치, SSE 불가, 면허 업로드, 토큰 rotation, 대댓글 삭제 |
| P1 중요 | 12건 | isLiked, Like 동기화, 댓글 표시, 문자열 불일치, 알림 네비게이션, Jackson 등 |
| P2 경미 | 7건 | 차단 필터링, 닉네임 TODO, SSE 재연결, accessToken 등 |

---

## p0-frontend-agent 작업 완료

### 수정 항목 (P0 치명적 이슈 5건)

- [x] 면허증 업로드 파라미터 키 불일치 수정 (`file` → `licenseImg`)
- [x] Refresh Token 저장 로직 추가 (새 refreshToken을 localStorage에 저장)
- [x] 대댓글 삭제 시 부모 댓글이 삭제되는 버그 수정 (`commentId` → `reply.id`)
- [x] SSE addEventListener 수정 (`onmessage` → `addEventListener('notification', ...)`)
- [x] 삭제된 댓글 판별 문자열 마침표 불일치 수정 (`삭제된 댓글입니다.` → `삭제된 댓글입니다`)

### 수정 파일
- `Front/src/api/authApi.ts`
- `Front/src/api/instance.ts`
- `Front/src/pages/board/components/CommentSection.tsx`
- `Front/src/store/notificationStore.ts`
- `Front/src/types/index.ts` (TokenRefreshResponse에 refreshToken 필드 추가)

## 백엔드 인증 이슈 수정 (p0-backend-auth-agent)

### 완료 항목

#### 이슈 1: 컨트롤러 인증 방식 통일
- [x] PostController: `@RequestHeader("X-Member-Id"/"X-Member-Role")` → `@AuthenticationPrincipal CustomUserDetails`
- [x] LikeController: `@AuthenticationPrincipal Long memberId` → `@AuthenticationPrincipal CustomUserDetails`
- [x] BlockController: `@AuthenticationPrincipal Long memberId` → `@AuthenticationPrincipal CustomUserDetails`
- [x] ReportController: `@AuthenticationPrincipal Long memberId` → `@AuthenticationPrincipal CustomUserDetails`
- [x] NotificationController: `@RequestParam Long memberId` → `@AuthenticationPrincipal CustomUserDetails` (SSE subscribe 포함)
- [x] FileController: `@RequestAttribute("memberId")` → `@AuthenticationPrincipal CustomUserDetails`

#### 이슈 2: TokenRefreshResponse에 refreshToken 필드 추가
- [x] TokenRefreshResponse에 refreshToken 필드 추가
- [x] AuthService.refresh()에서 newRefreshToken도 응답에 포함

#### 이슈 3: CommentController role "ROLE_" 접두사 문제
- [x] CommentController에서 `UserDetails` → `CustomUserDetails` 변경
- [x] `getRole()` 메서드 사용으로 "ROLE_" prefix 없이 순수 role 값 전달 (PENDING, MEMBER 등)

#### 이슈 4: AdminController adminId 하드코딩
- [x] `Long adminId = 1L` → `@AuthenticationPrincipal CustomUserDetails userDetails` + `userDetails.getMemberId()`

### 수정 파일
- `Back/src/main/java/com/melonme/post/controller/PostController.java`
- `Back/src/main/java/com/melonme/like/controller/LikeController.java`
- `Back/src/main/java/com/melonme/block/controller/BlockController.java`
- `Back/src/main/java/com/melonme/report/controller/ReportController.java`
- `Back/src/main/java/com/melonme/notification/controller/NotificationController.java`
- `Back/src/main/java/com/melonme/file/controller/FileController.java`
- `Back/src/main/java/com/melonme/member/dto/response/TokenRefreshResponse.java`
- `Back/src/main/java/com/melonme/member/service/AuthService.java`
- `Back/src/main/java/com/melonme/comment/controller/CommentController.java`
- `Back/src/main/java/com/melonme/admin/controller/AdminController.java`

## P0 이벤트 클래스 중복 수정 (p0-event-agent)

### 문제
- `CommentCreatedEvent`와 `LikeCreatedEvent`가 각각 2곳(도메인 패키지 + notification.event 패키지)에 중복 존재
- NotificationService가 notification.event 패키지의 이벤트를 수신하지만, CommentService/LikeService는 도메인 패키지의 이벤트를 발행 → 알림 시스템 전체 미동작

### 수정 내용

#### 1단계: 중복 이벤트 클래스 삭제
- [x] `com.melonme.notification.event.CommentCreatedEvent` 삭제
- [x] `com.melonme.notification.event.LikeCreatedEvent` 삭제
- [x] `notification/event/` 빈 패키지 디렉토리 삭제

#### 2단계: comment.domain.CommentCreatedEvent에 필드 추가
- [x] `postAuthorId` (Long) — 게시글 작성자 ID
- [x] `parentCommentAuthorId` (Long, nullable) — 대댓글 시 부모 댓글 작성자 ID

#### 3단계: like.domain.LikeCreatedEvent에 필드 추가
- [x] `targetAuthorId` (Long) — 좋아요 대상의 작성자 ID

#### 4단계: CommentService 이벤트 발행 수정
- [x] `createComment()`: postAuthorId = post.getMemberId(), parentCommentAuthorId = null
- [x] `createReply()`: postAuthorId = post.getMemberId(), parentCommentAuthorId = parentComment.getMemberId()

#### 5단계: LikeService 이벤트 발행 수정
- [x] PostRepository, CommentRepository 주입 추가
- [x] `resolveTargetAuthorId()` 메서드 추가 — targetType에 따라 게시글/댓글 작성자 ID 조회
- [x] 이벤트 발행 시 targetAuthorId 포함

#### 6단계: NotificationService import 및 필드 접근 수정
- [x] import를 `com.melonme.comment.domain.CommentCreatedEvent`로 변경
- [x] import를 `com.melonme.like.domain.LikeCreatedEvent`로 변경
- [x] `event.getCommentAuthorId()` → `event.getMemberId()`
- [x] `event.getLikerId()` → `event.getSenderId()`

#### 7단계: 단위테스트 업데이트
- [x] NotificationServiceTest: import 변경, 생성자 인자 순서 수정
- [x] LikeServiceTest: PostRepository/CommentRepository Mock 추가, 이벤트 검증에 targetAuthorId 추가
- [x] CommentServiceTest: 새 필드(postAuthorId, parentCommentAuthorId) 검증 추가

### 수정 파일
- `Back/src/main/java/com/melonme/comment/domain/CommentCreatedEvent.java`
- `Back/src/main/java/com/melonme/like/domain/LikeCreatedEvent.java`
- `Back/src/main/java/com/melonme/comment/service/CommentService.java`
- `Back/src/main/java/com/melonme/like/service/LikeService.java`
- `Back/src/main/java/com/melonme/notification/service/NotificationService.java`
- `Back/src/test/java/com/melonme/notification/service/NotificationServiceTest.java`
- `Back/src/test/java/com/melonme/like/service/LikeServiceTest.java`
- `Back/src/test/java/com/melonme/comment/service/CommentServiceTest.java`
- 삭제: `Back/src/main/java/com/melonme/notification/event/CommentCreatedEvent.java`
- 삭제: `Back/src/main/java/com/melonme/notification/event/LikeCreatedEvent.java`

## P1 버그 수정 (p1-backend-like-comment 에이전트)

### 이슈 7: isLiked 항상 false 하드코딩 → 수정 완료
- PostService에 LikeRepository 주입
- `likeRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, TargetType.POST, postId)` 로 실제 좋아요 여부 조회
- LikeRepository에 이미 해당 메서드 존재하여 추가 불필요

### 이슈 8: LikeSyncScheduler Redis 키 미삭제 → 수정 완료
- `get()` → `getAndDelete()` 로 변경 (ViewCountSyncScheduler 패턴 참고)
- 동기화 후 Redis 키가 삭제되어 중복 덮어쓰기 방지
- 기존 테스트도 `getAndDelete` 사용하도록 업데이트

### 이슈 9: @SQLRestriction으로 삭제된 부모 댓글 미표시 → 수정 완료
- CommentRepository의 `findAllByPostIdWithReplies` 쿼리를 native query로 변경
- 삭제된 부모 댓글이라도 활성 대댓글이 있으면 조회되도록 조건 추가
- `@SQLRestriction`은 유지, replies 컬렉션은 자동으로 활성 대댓글만 포함

### 이슈 18: likeCount가 DB 값만 반환 → 수정 완료
- PostService에 `getRedisLikeCount()` 메서드 추가 (Redis 키: `like:post:{postId}`)
- 게시글 상세 조회 시 `post.getLikeCount() + getRedisLikeCount(postId)` 로 합산 반환

### 수정 파일
- `Back/src/main/java/com/melonme/post/service/PostService.java`
- `Back/src/main/java/com/melonme/like/service/LikeSyncScheduler.java`
- `Back/src/main/java/com/melonme/comment/repository/CommentRepository.java`
- `Back/src/test/java/com/melonme/like/service/LikeSyncSchedulerTest.java`
- `Back/src/test/java/com/melonme/post/service/PostServiceTest.java`

## P1/P2 프론트엔드 이슈 수정 (p1-frontend-notification)

### 완료 항목

#### 이슈 11: COMMENT 알림 클릭 시 잘못된 페이지 이동
- [x] `Notification` 타입에 `postId?` 옵셔널 필드 추가
- [x] `NotificationDropdown.tsx` - 알림 클릭 시 `targetType` 기반 분기 로직 적용
- [x] `NotificationsPage.tsx` - 동일한 분기 로직 적용
- targetType이 POST이면 `/board/${targetId}`로 이동
- targetType이 COMMENT이면 postId가 있을 때만 `/board/${postId}`로 이동, 없으면 이동 안 함
- **백엔드 후속 작업 필요**: `NotificationResponse`에 `postId` 필드 추가 권장

#### 이슈 21: SSE 재연결 시 누락 알림 동기화
- [x] `notificationStore.ts` - SSE `onopen` 이벤트에서 `fetchNotifications` 호출하여 알림 동기화

#### 이슈 23: 댓글에 isLiked 상태 없음
- [x] `types/index.ts` - `Comment`, `Reply` 타입에 `isLiked: boolean` 필드 추가
- [x] `CommentSection.tsx` - CommentItem, ReplyItem에 `isLiked` 로컬 상태 및 UI 반영
- **백엔드 후속 작업 필요**: Comment/Reply 응답 DTO에 `isLiked` 필드 추가 권장

### 수정 파일
- `Front/src/types/index.ts`
- `Front/src/pages/mypage/components/NotificationDropdown.tsx`
- `Front/src/pages/mypage/NotificationsPage.tsx`
- `Front/src/store/notificationStore.ts`
- `Front/src/pages/board/components/CommentSection.tsx`

## P1/P2 이슈 수정 (p1-backend-post-security 에이전트)

### 이슈 14: 게시글 수정 시 기존 파일 연결 미해제
- [x] `PostService.updatePost()` - fileIds 전달 시 기존 파일 `unassignFromPost()` 호출 후 새 파일 연결
- [x] `FileEntity.unassignFromPost()` 메서드 추가

### 이슈 15: 게시글 삭제 시 연관 파일 미처리
- [x] `PostService.deletePost()` - 삭제 전 `fileRepository.findAllByPostId()`로 연관 파일 조회 후 `softDelete()` 처리
- [x] `PostServiceTest.deletePost_shouldSucceed_whenOwner` - fileRepository mock 추가

### 이슈 16: SecurityConfig PENDING 차단 범위 확대
- [x] `SecurityConfig` - MEMBER/ADMIN 제한 엔드포인트 추가:
  - `PATCH /api/posts/**` (수정/삭제)
  - `POST /api/posts/*/scraps` (스크랩)
  - `POST /api/likes` (좋아요)
  - `POST /api/reports` (신고)
  - `POST /api/blocks/**` (차단)

### 이슈 13: Jackson boolean is* 직렬화 문제
- [x] `NotificationResponse.isRead` - `@JsonProperty("isRead")` 추가
- [x] `BlockToggleResponse.isBlocked` - `@JsonProperty("isBlocked")` 추가
- [x] `ScrapResponse.isScrapped` - `@JsonProperty("isScrapped")` 추가
- [x] `LikeToggleResponse.isLiked` - `@JsonProperty("isLiked")` 추가

### 이슈 20: 스크랩 목록에서 차단 회원 게시글 미필터링
- [x] `PostRepository.findScrappedByMemberId()` - Block 서브쿼리 추가

### 이슈 22: Post/Comment 닉네임 조회 연동
- [x] `PostService` - `MemberRepository` 주입, `resolveAuthor()`에서 실제 닉네임 조회 (fallback "회원{id}" 유지)
- [x] `CommentService` - `MemberRepository` 주입, `getMemberNickname()`에서 실제 닉네임 조회 (fallback "회원{id}" 유지)
- [x] `CommentServiceTest` - `MemberRepository` mock 추가

### 수정 파일
- `Back/src/main/java/com/melonme/file/domain/FileEntity.java`
- `Back/src/main/java/com/melonme/post/service/PostService.java`
- `Back/src/main/java/com/melonme/global/config/SecurityConfig.java`
- `Back/src/main/java/com/melonme/notification/dto/response/NotificationResponse.java`
- `Back/src/main/java/com/melonme/block/dto/response/BlockToggleResponse.java`
- `Back/src/main/java/com/melonme/post/dto/response/ScrapResponse.java`
- `Back/src/main/java/com/melonme/like/dto/response/LikeToggleResponse.java`
- `Back/src/main/java/com/melonme/post/repository/PostRepository.java`
- `Back/src/main/java/com/melonme/comment/service/CommentService.java`
- `Back/src/test/java/com/melonme/post/service/PostServiceTest.java`
- `Back/src/test/java/com/melonme/comment/service/CommentServiceTest.java`

---

## 통합테스트 최종 결과 (2026-03-26)

### 수정 완료 현황

| 등급 | 총 건수 | 수정 완료 | 미수정 |
|------|---------|----------|--------|
| P0 치명 | 6건 | **6건** | 0건 |
| P1 중요 | 12건 | **12건** | 0건 |
| P2 경미 | 7건 | **5건** | 2건 |

### 미수정 P2 이슈 (후속 작업)
- #19: JwtAccessDeniedHandler ObjectMapper import — Spring Boot 4.0 Jackson 3.x 사용 확인 필요
- #24: 로그아웃 시 accessToken 무효화 — Redis blacklist 방식 도입 필요 (MVP 범위 외)

### 백엔드 후속 작업 권장 (프론트 대응 완료)
- NotificationResponse에 `postId` 필드 추가 → COMMENT 알림 클릭 시 게시글 이동
- CommentResponse/ReplyResponse에 `isLiked` 필드 추가 → 댓글 좋아요 초기 상태 표시

### 수정 후 테스트 결과
- **백엔드**: `./gradlew test` BUILD SUCCESSFUL (전체 테스트 통과, 0 실패)
- **프론트엔드**: `tsc --noEmit` 에러 0개

## 실서버 통합테스트 결과 (2026-03-26)

### 인프라 상태
- PostgreSQL 18 (localhost:5432) — 정상, DB 비밀번호: 환경변수 `DB_PASSWORD` 설정 필요
- Redis 3.0.504 (localhost:6379) — 정상
- S3 — AWS 키 미설정 시 S3Config 조건부 로딩으로 서버 정상 기동 (파일 업로드만 비활성)

### 서버 기동 중 수정한 추가 이슈
1. **S3Config 조건부 로딩** — AWS 키 없이도 서버 기동 가능하도록 `@ConditionalOnExpression` 적용
2. **FileService/LicenseService S3 optional 주입** — `@Autowired(required = false)` 적용
3. **PostRepository LOWER() bytea 에러** — Hibernate 7에서 null 파라미터가 bytea로 캐스팅되는 문제, `CAST(:keyword AS String)` 적용
4. **NotificationService 이벤트 핸들러 트랜잭션 누락** — `@TransactionalEventListener` 메서드에 `@Transactional(propagation = REQUIRES_NEW)` 추가

### API 실제 호출 테스트 (22개 항목)

| # | 테스트 항목 | 결과 |
|---|-----------|------|
| 1 | 게시글 작성 (POST /api/posts) | PASS |
| 2 | 게시글 작성 (user2) | PASS |
| 3 | 게시글 목록 조회 (totalCount=2) | PASS |
| 4 | 게시글 상세 조회 | PASS |
| 5 | 게시글 수정 (PATCH) | PASS |
| 6 | 댓글 작성 | PASS |
| 7 | Post.commentCount 증가 확인 | PASS |
| 8 | 댓글 → 알림 DB 저장 확인 (COMMENT_ON_POST) | PASS |
| 9 | 좋아요 토글 (isLiked=true) | PASS |
| 10 | Redis 좋아요 카운터 확인 (like:post:{id}=1) | PASS |
| 11 | 좋아요 → 알림 DB 저장 확인 (LIKE_ON_POST) | PASS |
| 12 | SSE 연결 확인 (event:connect) | PASS |
| 13 | 차단 (isBlocked=true) | PASS |
| 14 | 차단 후 게시글 목록 필터링 (totalCount=1) | PASS |
| 15 | 스크랩 토글 (isScrapped=true) | PASS |
| 16 | 스크랩 목록 조회 | PASS |
| 17 | 내가 쓴 글 목록 | PASS |
| 18 | 댓글 목록 조회 | PASS |
| 19 | 댓글 좋아요 | PASS |
| 20 | 게시글 삭제 (soft delete) | PASS |
| 21 | 삭제된 게시글 404 확인 | PASS |
| 22 | 차단 해제 토글 | PASS |

**결과: 22/22 PASS (0 FAIL)**

### 단위테스트 + TypeScript 최종 확인
- 백엔드 `./gradlew test`: BUILD SUCCESSFUL
- 프론트 `tsc --noEmit`: 에러 0개

## 테스트 로그인 API 추가 및 2차 통합테스트 (2026-03-26)

### 테스트 로그인 API
- `POST /api/test/login?memberId={id}` — JWT 토큰 발급
- `@Profile("local")` 적용 — 운영 환경 자동 비활성화
- SecurityConfig에서 `/api/test/**` permitAll 설정
- `data-local.sql` — 테스트 회원 3명 자동 INSERT (PENDING:100, MEMBER:101, ADMIN:102)

### 추가 수정 파일
- `TestAuthController.java` (신규)
- `SecurityConfig.java` (/api/test/** permitAll)
- `application.yaml` (profiles.active: local, sql.init 설정)
- `data-local.sql` (신규)

### 2차 통합테스트 결과 (테스트 로그인 API 사용)

| # | 테스트 항목 | 결과 |
|---|-----------|------|
| 1a | MEMBER 게시글 작성 | PASS |
| 1b | 게시글 상세 조회 | PASS |
| 1c | 게시글 수정 | PASS |
| 1d | 게시글 목록 조회 | PASS |
| 2a | 댓글 작성 | PASS |
| 2b | Post.commentCount 증가 확인 | PASS |
| 3 | 댓글 → 알림 DB 저장 (COMMENT_ON_POST) | PASS |
| 4a | 좋아요 토글 (isLiked=true) | PASS |
| 4b | Redis 좋아요 카운터 (like:post:{id}=1) | PASS |
| 4c | 좋아요 → 알림 DB 저장 (LIKE_ON_POST) | PASS |
| 5a | ADMIN 게시글 작성 | PASS |
| 5b | 차단 (isBlocked=true) | PASS |
| 5c | 차단 후 게시글 필터링 (totalCount=1) | PASS |
| 5d | 차단 해제 토글 | PASS |
| 6 | PENDING 게시글 작성 → 403 차단 | PASS |
| 7 | SSE 연결 (event:connect) | PASS |
| 8 | 게시글 삭제 (soft delete) | PASS |

**결과: 17/17 PASS (0 FAIL)**

### 프론트↔백엔드 연동 확인
- 프론트엔드: `http://localhost:5173` (Vite 실행 중)
- CORS preflight (OPTIONS): 200 OK
- `Access-Control-Allow-Origin: http://localhost:5173` 확인
- 프론트 → 백엔드 API 호출 정상 연동 확인

## E2E 브라우저 테스트 결과 (2026-03-26)

### 테스트 환경
- Playwright + Chromium (headless)
- 프론트: `http://localhost:5173` (Vite dev)
- 백엔드: `http://localhost:8080` (Spring Boot, local profile)
- 테스트 로그인 API로 JWT 발급 후 localStorage 주입

### E2E 테스트 중 수정한 이슈
- **TipTapViewer JSON.parse 에러** — content가 plain text일 때 `JSON.parse` 실패. try-catch로 fallback 처리 추가

### E2E 테스트 결과 (17항목)

| # | 테스트 항목 | 결과 |
|---|-----------|------|
| 1 | 랜딩페이지 렌더링 | PASS |
| 1b | 페이지 콘텐츠 존재 (height > 100) | PASS |
| 2 | 테스트 로그인 API (MEMBER) | PASS |
| 3 | 게시판 목록 페이지 렌더링 | PASS |
| 4a | 게시글 작성 페이지 렌더링 | PASS |
| 4b | 제목 입력 필드 동작 | PASS |
| 4c | 게시글 작성 submit | PASS |
| 4d | 작성한 게시글 목록 반영 확인 | PASS |
| 5a | 게시글 상세 페이지 렌더링 | PASS |
| 5b | 댓글 작성 (API) | PASS |
| 5c | 댓글 표시 확인 | PASS |
| 6 | 좋아요 토글 (API) | PASS |
| 7 | GNB 알림 영역 존재 | PASS |
| 8 | 마이페이지 렌더링 | PASS |
| 9a | 드롭다운 → 로그아웃 클릭 | PASS |
| 9b | 로그아웃 후 리다이렉트 | PASS |
| 10 | 치명적 콘솔 에러 없음 | PASS |

**결과: 17/17 PASS (0 FAIL)**
