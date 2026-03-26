package com.melonme.notification.service;

import com.melonme.notification.domain.NotificationTargetType;
import com.melonme.notification.domain.NotificationType;
import com.melonme.notification.dto.response.NotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterServiceTest {

    private final SseEmitterService sseEmitterService = new SseEmitterService();

    @Test
    @DisplayName("SSE 구독 시 emitter가 Map에 등록된다")
    void shouldRegisterEmitterOnSubscribe() {
        // given
        Long memberId = 1L;

        // when
        SseEmitter emitter = sseEmitterService.subscribe(memberId);

        // then
        assertThat(emitter).isNotNull();
        assertThat(sseEmitterService.hasEmitter(memberId)).isTrue();
    }

    @Test
    @DisplayName("같은 회원이 재구독하면 기존 emitter가 교체된다")
    void shouldReplaceExistingEmitterOnResubscribe() {
        // given
        Long memberId = 1L;
        SseEmitter firstEmitter = sseEmitterService.subscribe(memberId);

        // when
        SseEmitter secondEmitter = sseEmitterService.subscribe(memberId);

        // then
        assertThat(secondEmitter).isNotSameAs(firstEmitter);
        assertThat(sseEmitterService.getEmitters()).hasSize(1);
    }

    @Test
    @DisplayName("연결되지 않은 회원에게 알림 전송 시 예외가 발생하지 않는다")
    void shouldNotThrowWhenSendingToDisconnectedMember() {
        // given
        Long memberId = 999L;
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.COMMENT_ON_POST)
                .targetType(NotificationTargetType.POST)
                .targetId(100L)
                .message("테스트 알림")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        // when & then (no exception)
        sseEmitterService.sendNotification(memberId, response);
    }

    @Test
    @DisplayName("hasEmitter는 연결되지 않은 회원에 대해 false를 반환한다")
    void shouldReturnFalseForDisconnectedMember() {
        // given
        Long memberId = 999L;

        // when & then
        assertThat(sseEmitterService.hasEmitter(memberId)).isFalse();
    }
}
