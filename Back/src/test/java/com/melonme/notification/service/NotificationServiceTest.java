package com.melonme.notification.service;

import com.melonme.block.repository.BlockRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.like.domain.TargetType;
import com.melonme.notification.domain.Notification;
import com.melonme.notification.domain.NotificationTargetType;
import com.melonme.notification.domain.NotificationType;
import com.melonme.notification.dto.response.NotificationListResponse;
import com.melonme.comment.domain.CommentCreatedEvent;
import com.melonme.like.domain.LikeCreatedEvent;
import com.melonme.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private SseEmitterService sseEmitterService;

    @Nested
    @DisplayName("댓글 이벤트 처리")
    class HandleCommentCreated {

        @Test
        @DisplayName("본인 행위는 알림을 발송하지 않는다")
        void shouldNotNotifySelfAction() {
            // given
            Long memberId = 1L;
            CommentCreatedEvent event = new CommentCreatedEvent(
                    200L, 100L, memberId, null, memberId, null
            );

            // when
            notificationService.handleCommentCreated(event);

            // then
            verify(notificationRepository, never()).save(any());
            verify(sseEmitterService, never()).sendNotification(any(), any());
        }

        @Test
        @DisplayName("차단 회원의 알림은 발송하지 않는다")
        void shouldNotNotifyBlockedMember() {
            // given
            Long postAuthorId = 1L;
            Long commentAuthorId = 2L;
            CommentCreatedEvent event = new CommentCreatedEvent(
                    200L, 100L, commentAuthorId, null, postAuthorId, null
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(postAuthorId, commentAuthorId))
                    .willReturn(true);

            // when
            notificationService.handleCommentCreated(event);

            // then
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("일반 댓글 작성 시 게시글 작성자에게 COMMENT_ON_POST 알림을 발송한다")
        void shouldSendCommentOnPostNotification() {
            // given
            Long postAuthorId = 1L;
            Long commentAuthorId = 2L;
            Long postId = 100L;
            Long commentId = 200L;
            CommentCreatedEvent event = new CommentCreatedEvent(
                    commentId, postId, commentAuthorId, null, postAuthorId, null
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(postAuthorId, commentAuthorId))
                    .willReturn(false);
            given(notificationRepository.save(any(Notification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            notificationService.handleCommentCreated(event);

            // then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getReceiverId()).isEqualTo(postAuthorId);
            assertThat(saved.getSenderId()).isEqualTo(commentAuthorId);
            assertThat(saved.getType()).isEqualTo(NotificationType.COMMENT_ON_POST);
            assertThat(saved.getTargetType()).isEqualTo(NotificationTargetType.POST);
            assertThat(saved.getTargetId()).isEqualTo(postId);
        }

        @Test
        @DisplayName("대댓글 작성 시 부모 댓글 작성자에게 REPLY_ON_COMMENT 알림을 발송한다")
        void shouldSendReplyOnCommentNotification() {
            // given
            Long postAuthorId = 1L;
            Long commentAuthorId = 3L;
            Long parentCommentAuthorId = 2L;
            Long commentId = 300L;
            Long parentCommentId = 250L;
            CommentCreatedEvent event = new CommentCreatedEvent(
                    commentId, 100L, commentAuthorId, parentCommentId, postAuthorId, parentCommentAuthorId
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(parentCommentAuthorId, commentAuthorId))
                    .willReturn(false);
            given(notificationRepository.save(any(Notification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            notificationService.handleCommentCreated(event);

            // then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getReceiverId()).isEqualTo(parentCommentAuthorId);
            assertThat(saved.getType()).isEqualTo(NotificationType.REPLY_ON_COMMENT);
            assertThat(saved.getTargetType()).isEqualTo(NotificationTargetType.COMMENT);
            assertThat(saved.getTargetId()).isEqualTo(commentId);
        }
    }

    @Nested
    @DisplayName("좋아요 이벤트 처리")
    class HandleLikeCreated {

        @Test
        @DisplayName("본인 좋아요는 알림을 발송하지 않는다")
        void shouldNotNotifySelfLike() {
            // given
            Long memberId = 1L;
            LikeCreatedEvent event = new LikeCreatedEvent(
                    memberId, TargetType.POST, 100L, memberId
            );

            // when
            notificationService.handleLikeCreated(event);

            // then
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("차단 회원의 좋아요 알림은 발송하지 않는다")
        void shouldNotNotifyBlockedMemberLike() {
            // given
            Long targetAuthorId = 1L;
            Long likerId = 2L;
            LikeCreatedEvent event = new LikeCreatedEvent(
                    likerId, TargetType.POST, 100L, targetAuthorId
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(targetAuthorId, likerId))
                    .willReturn(true);

            // when
            notificationService.handleLikeCreated(event);

            // then
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("게시글 좋아요 시 LIKE_ON_POST 알림을 발송한다")
        void shouldSendLikeOnPostNotification() {
            // given
            Long targetAuthorId = 1L;
            Long likerId = 2L;
            Long postId = 100L;
            LikeCreatedEvent event = new LikeCreatedEvent(
                    likerId, TargetType.POST, postId, targetAuthorId
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(targetAuthorId, likerId))
                    .willReturn(false);
            given(notificationRepository.save(any(Notification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            notificationService.handleLikeCreated(event);

            // then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(NotificationType.LIKE_ON_POST);
            assertThat(saved.getTargetType()).isEqualTo(NotificationTargetType.POST);
            assertThat(saved.getTargetId()).isEqualTo(postId);
        }

        @Test
        @DisplayName("댓글 좋아요 시 LIKE_ON_COMMENT 알림을 발송한다")
        void shouldSendLikeOnCommentNotification() {
            // given
            Long targetAuthorId = 1L;
            Long likerId = 2L;
            Long commentId = 200L;
            LikeCreatedEvent event = new LikeCreatedEvent(
                    likerId, TargetType.COMMENT, commentId, targetAuthorId
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(targetAuthorId, likerId))
                    .willReturn(false);
            given(notificationRepository.save(any(Notification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            notificationService.handleLikeCreated(event);

            // then
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(NotificationType.LIKE_ON_COMMENT);
            assertThat(saved.getTargetType()).isEqualTo(NotificationTargetType.COMMENT);
        }
    }

    @Nested
    @DisplayName("SSE 연결 없을 때")
    class WhenNoSseConnection {

        @Test
        @DisplayName("SSE 연결이 없어도 DB에는 알림이 저장된다")
        void shouldSaveToDbWithoutSseConnection() {
            // given
            Long postAuthorId = 1L;
            Long commentAuthorId = 2L;
            CommentCreatedEvent event = new CommentCreatedEvent(
                    200L, 100L, commentAuthorId, null, postAuthorId, null
            );
            given(blockRepository.existsByBlockerIdAndBlockedId(postAuthorId, commentAuthorId))
                    .willReturn(false);
            given(notificationRepository.save(any(Notification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            notificationService.handleCommentCreated(event);

            // then
            verify(notificationRepository).save(any(Notification.class));
            verify(sseEmitterService).sendNotification(eq(postAuthorId), any());
        }
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class GetNotifications {

        @Test
        @DisplayName("페이지네이션으로 알림 목록을 조회한다")
        void shouldReturnPaginatedNotifications() {
            // given
            Long memberId = 1L;
            Notification notification = Notification.builder()
                    .receiverId(memberId)
                    .senderId(2L)
                    .type(NotificationType.COMMENT_ON_POST)
                    .targetType(NotificationTargetType.POST)
                    .targetId(100L)
                    .build();

            Page<Notification> page = new PageImpl<>(
                    List.of(notification), PageRequest.of(0, 20), 1
            );

            given(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(eq(memberId), any(PageRequest.class)))
                    .willReturn(page);
            given(notificationRepository.countByReceiverIdAndIsReadFalse(memberId))
                    .willReturn(1L);

            // when
            NotificationListResponse response = notificationService.getNotifications(memberId, 0, 20);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getUnreadCount()).isEqualTo(1);
            assertThat(response.isHasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("개별 알림을 읽음 처리한다")
        void shouldMarkSingleNotificationAsRead() {
            // given
            Long memberId = 1L;
            Long notificationId = 10L;
            Notification notification = Notification.builder()
                    .receiverId(memberId)
                    .senderId(2L)
                    .type(NotificationType.COMMENT_ON_POST)
                    .targetType(NotificationTargetType.POST)
                    .targetId(100L)
                    .build();

            given(notificationRepository.findById(notificationId))
                    .willReturn(Optional.of(notification));

            // when
            notificationService.markAsRead(notificationId, memberId);

            // then
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("다른 사용자의 알림은 읽음 처리할 수 없다")
        void shouldThrowWhenMarkingOthersNotification() {
            // given
            Long ownerId = 1L;
            Long otherMemberId = 2L;
            Long notificationId = 10L;
            Notification notification = Notification.builder()
                    .receiverId(ownerId)
                    .senderId(3L)
                    .type(NotificationType.COMMENT_ON_POST)
                    .targetType(NotificationTargetType.POST)
                    .targetId(100L)
                    .build();

            given(notificationRepository.findById(notificationId))
                    .willReturn(Optional.of(notification));

            // when & then
            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, otherMemberId))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("전체 알림을 읽음 처리한다")
        void shouldMarkAllNotificationsAsRead() {
            // given
            Long memberId = 1L;

            // when
            notificationService.markAllAsRead(memberId);

            // then
            verify(notificationRepository).markAllAsRead(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 알림은 읽음 처리할 수 없다")
        void shouldThrowWhenNotificationNotFound() {
            // given
            Long notificationId = 999L;
            Long memberId = 1L;
            given(notificationRepository.findById(notificationId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.markAsRead(notificationId, memberId))
                    .isInstanceOf(CustomException.class);
        }
    }
}
