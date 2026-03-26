package com.melonme.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationListResponse {

    private List<NotificationResponse> content;
    private long unreadCount;
    private boolean hasNext;

    public static NotificationListResponse of(List<NotificationResponse> content, long unreadCount, boolean hasNext) {
        return new NotificationListResponse(content, unreadCount, hasNext);
    }
}
