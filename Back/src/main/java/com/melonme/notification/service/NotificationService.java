package com.melonme.notification.service;

import com.melonme.block.repository.BlockRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.like.domain.TargetType;
import com.melonme.notification.domain.Notification;
import com.melonme.notification.domain.NotificationTargetType;
import com.melonme.notification.domain.NotificationType;
import com.melonme.notification.dto.response.NotificationListResponse;
import com.melonme.notification.dto.response.NotificationResponse;
import com.melonme.comment.domain.CommentCreatedEvent;
import com.melonme.like.domain.LikeCreatedEvent;
import com.melonme.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BlockRepository blockRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long memberId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(memberId, pageRequest);

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(NotificationResponse::from)
                .toList();

        long unreadCount = notificationRepository.countByReceiverIdAndIsReadFalse(memberId);

        return NotificationListResponse.of(content, unreadCount, notificationPage.hasNext());
    }

    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!notification.getReceiverId().equals(memberId)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentCreated(CommentCreatedEvent event) {
        Long senderId = event.getMemberId();

        if (event.getParentCommentAuthorId() != null) {
            // 대댓글 → 부모 댓글 작성자에게 REPLY_ON_COMMENT 알림
            Long receiverId = event.getParentCommentAuthorId();
            createAndSendNotification(
                    receiverId, senderId,
                    NotificationType.REPLY_ON_COMMENT,
                    NotificationTargetType.COMMENT,
                    event.getCommentId()
            );
        } else {
            // 일반 댓글 → 게시글 작성자에게 COMMENT_ON_POST 알림
            Long receiverId = event.getPostAuthorId();
            createAndSendNotification(
                    receiverId, senderId,
                    NotificationType.COMMENT_ON_POST,
                    NotificationTargetType.POST,
                    event.getPostId()
            );
        }
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLikeCreated(LikeCreatedEvent event) {
        Long senderId = event.getSenderId();
        Long receiverId = event.getTargetAuthorId();

        NotificationType type;
        NotificationTargetType targetType;

        if (event.getTargetType() == TargetType.POST) {
            type = NotificationType.LIKE_ON_POST;
            targetType = NotificationTargetType.POST;
        } else {
            type = NotificationType.LIKE_ON_COMMENT;
            targetType = NotificationTargetType.COMMENT;
        }

        createAndSendNotification(receiverId, senderId, type, targetType, event.getTargetId());
    }

    private void createAndSendNotification(Long receiverId, Long senderId,
                                           NotificationType type,
                                           NotificationTargetType targetType,
                                           Long targetId) {
        // 본인 행위는 알림 미발송
        if (receiverId.equals(senderId)) {
            log.debug("Skipping notification: self-action by memberId={}", senderId);
            return;
        }

        // 차단 회원 알림 미발송 (수신자가 발신자를 차단한 경우)
        if (blockRepository.existsByBlockerIdAndBlockedId(receiverId, senderId)) {
            log.debug("Skipping notification: receiver={} blocked sender={}", receiverId, senderId);
            return;
        }

        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        notificationRepository.save(notification);

        // SSE 연결 중이면 즉시 전송
        NotificationResponse response = NotificationResponse.from(notification);
        sseEmitterService.sendNotification(receiverId, response);
    }
}
