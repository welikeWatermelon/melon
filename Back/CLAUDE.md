# 백엔드 공통 규칙

## 기술 스택
- Spring Boot + Java
- PostgreSQL
- Redis
- AWS S3
- SSE (Server-Sent Events)

## 패키지 구조
```
src/main/java/com/project/
├── global/
│   ├── config/
│   │   ├── SecurityConfig.java      ← member-agent 전담
│   │   ├── RedisConfig.java
│   │   ├── S3Config.java
│   │   └── SseConfig.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── CustomException.java
│   │   └── ErrorCode.java
│   ├── response/
│   │   └── ApiResponse.java
│   └── util/
├── member/
├── post/
├── comment/
├── like/
├── file/
├── report/
├── block/
├── notification/
└── admin/
```

## 도메인 내부 구조 (모든 도메인 동일하게 적용)
```
{domain}/
├── controller/
├── service/
├── repository/
├── domain/          ← Entity
├── dto/
│   ├── request/
│   └── response/
└── exception/       ← 도메인 전용 예외
```

## 공통 응답 형식
```java
// 성공
ApiResponse.success(data)
→ { "success": true, "data": { } }

// 실패
ApiResponse.fail(errorCode)
→ { "success": false, "code": "", "message": "" }
```

## 예외처리 규칙
- 모든 예외는 CustomException 상속
- ErrorCode enum으로 관리
- GlobalExceptionHandler에서 일괄 처리
- 도메인별 예외는 각 도메인 exception/ 에 정의

## Security 규칙
- SecurityConfig는 member-agent 전담
- 다른 에이전트는 SecurityConfig 절대 수정 금지
- 인증 필요한 API는 @PreAuthorize 또는 SecurityConfig에서 설정

## Soft Delete 규칙
- 삭제 = deleted_at 업데이트
- 모든 조회 쿼리에 deleted_at IS NULL 조건 필수
- JPA: @Where(clause = "deleted_at IS NULL") 사용

## JPA 규칙
- 지연 로딩(LAZY) 기본
- N+1 문제 주의 → fetch join 또는 @EntityGraph 사용
- Querydsl 사용 (복잡한 조회 쿼리)

## 트랜잭션 규칙
- Service 레이어에 @Transactional 적용
- 조회는 @Transactional(readOnly = true)
- 이벤트 발행은 트랜잭션 커밋 후 실행
  (@TransactionalEventListener 사용)

## Redis 규칙
- 키 형식: {prefix}:{id}
- TTL 반드시 설정
- RedisConfig.java에서 직렬화 설정 (JSON)

## 단위테스트 규칙
- Service 레이어 단위테스트 필수
- Mockito로 의존성 Mock 처리
- given/when/then 형식 준수
- 테스트 파일: {ClassName}Test.java

## ApplicationEvent 규칙
- 도메인 간 의존성은 이벤트로 처리
- Like → Notification: LikeCreatedEvent
- Comment → Notification: CommentCreatedEvent
- 이벤트 발행: ApplicationEventPublisher
- 이벤트 수신: @TransactionalEventListener