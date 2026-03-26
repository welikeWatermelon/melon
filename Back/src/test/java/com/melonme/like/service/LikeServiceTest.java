package com.melonme.like.service;

import com.melonme.comment.domain.Comment;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.like.domain.Like;
import com.melonme.like.domain.LikeCreatedEvent;
import com.melonme.like.domain.TargetType;
import com.melonme.like.dto.request.LikeToggleRequest;
import com.melonme.like.dto.response.LikeToggleResponse;
import com.melonme.like.repository.LikeRepository;
import com.melonme.post.domain.Post;
import com.melonme.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private static final Long MEMBER_ID = 1L;
    private static final Long POST_ID = 100L;
    private static final Long COMMENT_ID = 200L;
    private static final Long POST_AUTHOR_ID = 10L;
    private static final Long COMMENT_AUTHOR_ID = 20L;

    @Test
    @DisplayName("게시글 좋아요 추가 - 좋아요가 없으면 추가하고 Redis +1")
    void toggle_addLike_post() {
        // given
        LikeToggleRequest request = new LikeToggleRequest(TargetType.POST, POST_ID);
        String redisKey = "like:post:" + POST_ID;

        Post post = Post.builder().memberId(POST_AUTHOR_ID).title("t").content("c").build();
        given(likeRepository.findByMemberIdAndTargetTypeAndTargetId(MEMBER_ID, TargetType.POST, POST_ID))
                .willReturn(Optional.empty());
        given(likeRepository.save(any(Like.class)))
                .willReturn(Like.builder().memberId(MEMBER_ID).targetType(TargetType.POST).targetId(POST_ID).build());
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(redisKey)).willReturn(1L);
        given(valueOperations.get(redisKey)).willReturn(1);

        // when
        LikeToggleResponse response = likeService.toggle(MEMBER_ID, request);

        // then
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1L);
        verify(likeRepository).save(any(Like.class));
        verify(valueOperations).increment(redisKey);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 좋아요가 있으면 삭제하고 Redis -1")
    void toggle_removeLike_post() {
        // given
        LikeToggleRequest request = new LikeToggleRequest(TargetType.POST, POST_ID);
        String redisKey = "like:post:" + POST_ID;
        Like existingLike = Like.builder()
                .memberId(MEMBER_ID)
                .targetType(TargetType.POST)
                .targetId(POST_ID)
                .build();

        given(likeRepository.findByMemberIdAndTargetTypeAndTargetId(MEMBER_ID, TargetType.POST, POST_ID))
                .willReturn(Optional.of(existingLike));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.decrement(redisKey)).willReturn(0L);
        given(valueOperations.get(redisKey)).willReturn(0);

        // when
        LikeToggleResponse response = likeService.toggle(MEMBER_ID, request);

        // then
        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikeCount()).isEqualTo(0L);
        verify(likeRepository).delete(existingLike);
        verify(valueOperations).decrement(redisKey);
    }

    @Test
    @DisplayName("댓글 좋아요 추가 - COMMENT 타입으로 Redis 키가 생성된다")
    void toggle_addLike_comment() {
        // given
        LikeToggleRequest request = new LikeToggleRequest(TargetType.COMMENT, COMMENT_ID);
        String redisKey = "like:comment:" + COMMENT_ID;

        Comment comment = Comment.builder().postId(POST_ID).memberId(COMMENT_AUTHOR_ID).content("c").build();
        given(likeRepository.findByMemberIdAndTargetTypeAndTargetId(MEMBER_ID, TargetType.COMMENT, COMMENT_ID))
                .willReturn(Optional.empty());
        given(likeRepository.save(any(Like.class)))
                .willReturn(Like.builder().memberId(MEMBER_ID).targetType(TargetType.COMMENT).targetId(COMMENT_ID).build());
        given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(redisKey)).willReturn(5L);
        given(valueOperations.get(redisKey)).willReturn(5);

        // when
        LikeToggleResponse response = likeService.toggle(MEMBER_ID, request);

        // then
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(5L);
        verify(valueOperations).increment(redisKey);
    }

    @Test
    @DisplayName("좋아요 추가 시 LikeCreatedEvent가 발행된다")
    void toggle_addLike_publishesEvent() {
        // given
        LikeToggleRequest request = new LikeToggleRequest(TargetType.POST, POST_ID);
        String redisKey = "like:post:" + POST_ID;

        Post post = Post.builder().memberId(POST_AUTHOR_ID).title("t").content("c").build();
        given(likeRepository.findByMemberIdAndTargetTypeAndTargetId(MEMBER_ID, TargetType.POST, POST_ID))
                .willReturn(Optional.empty());
        given(likeRepository.save(any(Like.class)))
                .willReturn(Like.builder().memberId(MEMBER_ID).targetType(TargetType.POST).targetId(POST_ID).build());
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(redisKey)).willReturn(1L);
        given(valueOperations.get(redisKey)).willReturn(1);

        // when
        likeService.toggle(MEMBER_ID, request);

        // then
        ArgumentCaptor<LikeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(LikeCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        LikeCreatedEvent event = eventCaptor.getValue();
        assertThat(event.getSenderId()).isEqualTo(MEMBER_ID);
        assertThat(event.getTargetType()).isEqualTo(TargetType.POST);
        assertThat(event.getTargetId()).isEqualTo(POST_ID);
        assertThat(event.getTargetAuthorId()).isEqualTo(POST_AUTHOR_ID);
    }

    @Test
    @DisplayName("좋아요 취소 시 이벤트가 발행되지 않는다")
    void toggle_removeLike_doesNotPublishEvent() {
        // given
        LikeToggleRequest request = new LikeToggleRequest(TargetType.POST, POST_ID);
        String redisKey = "like:post:" + POST_ID;
        Like existingLike = Like.builder()
                .memberId(MEMBER_ID)
                .targetType(TargetType.POST)
                .targetId(POST_ID)
                .build();

        given(likeRepository.findByMemberIdAndTargetTypeAndTargetId(MEMBER_ID, TargetType.POST, POST_ID))
                .willReturn(Optional.of(existingLike));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.decrement(redisKey)).willReturn(0L);
        given(valueOperations.get(redisKey)).willReturn(0);

        // when
        likeService.toggle(MEMBER_ID, request);

        // then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Redis 값이 null일 때 likeCount는 0을 반환한다")
    void toggle_redisNullValue_returnsZero() {
        // given
        LikeToggleRequest request = new LikeToggleRequest(TargetType.POST, POST_ID);
        String redisKey = "like:post:" + POST_ID;

        Post post = Post.builder().memberId(POST_AUTHOR_ID).title("t").content("c").build();
        given(likeRepository.findByMemberIdAndTargetTypeAndTargetId(MEMBER_ID, TargetType.POST, POST_ID))
                .willReturn(Optional.empty());
        given(likeRepository.save(any(Like.class)))
                .willReturn(Like.builder().memberId(MEMBER_ID).targetType(TargetType.POST).targetId(POST_ID).build());
        given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(redisKey)).willReturn(1L);
        given(valueOperations.get(redisKey)).willReturn(null);

        // when
        LikeToggleResponse response = likeService.toggle(MEMBER_ID, request);

        // then
        assertThat(response.getLikeCount()).isEqualTo(0L);
    }
}
