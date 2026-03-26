package com.melonme.notification.service;

import com.melonme.notification.dto.response.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 1800000L; // 30분

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long memberId) {
        // 기존 연결이 있으면 제거
        removeEmitter(memberId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("SSE connection completed for memberId={}", memberId);
            emitters.remove(memberId);
        });
        emitter.onTimeout(() -> {
            log.info("SSE connection timed out for memberId={}", memberId);
            emitters.remove(memberId);
        });
        emitter.onError(throwable -> {
            log.warn("SSE connection error for memberId={}: {}", memberId, throwable.getMessage());
            emitters.remove(memberId);
        });

        emitters.put(memberId, emitter);

        // 더미 이벤트 전송 (503 방지)
        sendDummyEvent(emitter);

        return emitter;
    }

    public void sendNotification(Long receiverId, NotificationResponse response) {
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(response));
            } catch (IOException e) {
                log.warn("Failed to send SSE notification to memberId={}: {}", receiverId, e.getMessage());
                removeEmitter(receiverId);
            }
        }
    }

    public boolean hasEmitter(Long memberId) {
        return emitters.containsKey(memberId);
    }

    private void sendDummyEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            log.warn("Failed to send dummy event: {}", e.getMessage());
        }
    }

    private void removeEmitter(Long memberId) {
        SseEmitter existing = emitters.remove(memberId);
        if (existing != null) {
            existing.complete();
        }
    }

    // 테스트용
    Map<Long, SseEmitter> getEmitters() {
        return emitters;
    }
}
