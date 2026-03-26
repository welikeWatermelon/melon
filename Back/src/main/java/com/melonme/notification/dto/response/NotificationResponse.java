package com.melonme.notification.dto.response;

import com.melonme.notification.domain.Notification;
import com.melonme.notification.domain.NotificationTargetType;
import com.melonme.notification.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private NotificationTargetType targetType;
    private Long targetId;
    private String message;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .message(generateMessage(notification.getType()))
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private static String generateMessage(NotificationType type) {
        return switch (type) {
            case COMMENT_ON_POST -> "게시글에 새 댓글이 달렸습니다.";
            case REPLY_ON_COMMENT -> "댓글에 새 대댓글이 달렸습니다.";
            case LIKE_ON_POST -> "게시글에 좋아요가 눌렸습니다.";
            case LIKE_ON_COMMENT -> "댓글에 좋아요가 눌렸습니다.";
        };
    }
}
