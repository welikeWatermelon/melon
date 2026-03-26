package com.melonme.like.service;

import com.melonme.like.repository.LikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class LikeSyncSchedulerTest {

    @InjectMocks
    private LikeSyncScheduler likeSyncScheduler;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Test
    @DisplayName("배치 - 게시글 좋아요 카운트를 DB에 동기화한다")
    void syncLikeCounts_post() {
        // given
        Set<String> postKeys = Set.of("like:post:1", "like:post:2");
        given(redisTemplate.keys("like:post:*")).willReturn(postKeys);
        given(redisTemplate.keys("like:comment:*")).willReturn(Set.of());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.getAndDelete("like:post:1")).willReturn(10);
        given(valueOperations.getAndDelete("like:post:2")).willReturn(5);

        // when
        likeSyncScheduler.syncLikeCountsToDatabase();

        // then
        verify(likeRepository).updatePostLikeCount(1L, 10);
        verify(likeRepository).updatePostLikeCount(2L, 5);
    }

    @Test
    @DisplayName("배치 - 댓글 좋아요 카운트를 DB에 동기화한다")
    void syncLikeCounts_comment() {
        // given
        Set<String> commentKeys = Set.of("like:comment:100");
        given(redisTemplate.keys("like:post:*")).willReturn(Set.of());
        given(redisTemplate.keys("like:comment:*")).willReturn(commentKeys);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.getAndDelete("like:comment:100")).willReturn(3);

        // when
        likeSyncScheduler.syncLikeCountsToDatabase();

        // then
        verify(likeRepository).updateCommentLikeCount(100L, 3);
    }

    @Test
    @DisplayName("배치 - Redis 키가 없으면 DB 업데이트를 하지 않는다")
    void syncLikeCounts_noKeys() {
        // given
        given(redisTemplate.keys("like:post:*")).willReturn(Set.of());
        given(redisTemplate.keys("like:comment:*")).willReturn(Set.of());

        // when
        likeSyncScheduler.syncLikeCountsToDatabase();

        // then
        verify(likeRepository, never()).updatePostLikeCount(anyLong(), anyInt());
        verify(likeRepository, never()).updateCommentLikeCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("배치 - Redis 키가 null이면 DB 업데이트를 하지 않는다")
    void syncLikeCounts_nullKeys() {
        // given
        given(redisTemplate.keys("like:post:*")).willReturn(null);
        given(redisTemplate.keys("like:comment:*")).willReturn(null);

        // when
        likeSyncScheduler.syncLikeCountsToDatabase();

        // then
        verify(likeRepository, never()).updatePostLikeCount(anyLong(), anyInt());
        verify(likeRepository, never()).updateCommentLikeCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("배치 - Redis 값이 null이면 동기화를 건너뛴다")
    void syncLikeCounts_nullValue_skip() {
        // given
        Set<String> postKeys = Set.of("like:post:1");
        given(redisTemplate.keys("like:post:*")).willReturn(postKeys);
        given(redisTemplate.keys("like:comment:*")).willReturn(Set.of());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.getAndDelete("like:post:1")).willReturn(null);

        // when
        likeSyncScheduler.syncLikeCountsToDatabase();

        // then
        verify(likeRepository, never()).updatePostLikeCount(anyLong(), anyInt());
    }
}
