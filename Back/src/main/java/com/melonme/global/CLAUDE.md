# Global 패키지

## 담당 범위
- 모든 도메인에서 공통으로 사용하는 설정, 예외, 응답, 베이스 엔티티

## 구성

### config/
- SecurityConfig.java - Spring Security 설정 (member-agent 전담, 다른 에이전트 수정 금지)
- RedisConfig.java - RedisTemplate JSON 직렬화 설정
- S3Config.java - AWS SDK v2 S3Client Bean
- SseConfig.java - SSE AsyncSupport 타임아웃 (30분)
- JpaConfig.java - @EnableJpaAuditing

### exception/
- ErrorCode.java - 에러코드 enum (HttpStatus + code + message)
- CustomException.java - RuntimeException 상속, ErrorCode 보유
- GlobalExceptionHandler.java - @RestControllerAdvice 일괄 예외처리

### response/
- ApiResponse.java - 공통 응답 래퍼 (success/fail 정적 팩토리)

### domain/
- BaseTimeEntity.java - createdAt, updatedAt, deletedAt + softDelete()

## 사용 규칙
- 모든 엔티티는 BaseTimeEntity 상속
- 모든 예외는 CustomException(ErrorCode) 사용
- 모든 API 응답은 ApiResponse로 래핑
- 도메인별 ErrorCode는 ErrorCode enum에 추가
