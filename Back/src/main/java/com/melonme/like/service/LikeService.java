package com.melonme.like.service;

import com.melonme.comment.domain.Comment;
import com.melonme.comment.repository.CommentRepository;
import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.like.domain.Like;
import com.melonme.like.domain.LikeCreatedEvent;
import com.melonme.like.domain.TargetType;
import com.melonme.like.dto.request.LikeToggleRequest;
import com.melonme.like.dto.response.LikeToggleResponse;
import com.melonme.like.repository.LikeRepository;
import com.melonme.post.domain.Post;
import com.melonme.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final String LIKE_POST_KEY_PREFIX = "like:post:";
    private static final String LIKE_COMMENT_KEY_PREFIX = "like:comment:";

    @Transactional
    public LikeToggleResponse toggle(Long memberId, LikeToggleRequest request) {
        TargetType targetType = request.getTargetType();
        Long targetId = request.getTargetId();

        Optional<Like> existingLike = likeRepository
                .findByMemberIdAndTargetTypeAndTargetId(memberId, targetType, targetId);

        String redisKey = buildRedisKey(targetType, targetId);
        boolean isLiked;

        if (existingLike.isPresent()) {
            // 좋아요 취소
            likeRepository.delete(existingLike.get());
            redisTemplate.opsForValue().decrement(redisKey);
            isLiked = false;
        } else {
            // 좋아요 추가
            Like like = Like.builder()
                    .memberId(memberId)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            likeRepository.save(like);
            redisTemplate.opsForValue().increment(redisKey);
            isLiked = true;

            // 이벤트 발행
            Long targetAuthorId = resolveTargetAuthorId(targetType, targetId);
            eventPublisher.publishEvent(new LikeCreatedEvent(memberId, targetType, targetId, targetAuthorId));
        }

        long likeCount = getLikeCount(redisKey);
        return new LikeToggleResponse(isLiked, likeCount);
    }

    private String buildRedisKey(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> LIKE_POST_KEY_PREFIX + targetId;
            case COMMENT -> LIKE_COMMENT_KEY_PREFIX + targetId;
        };
    }

    private Long resolveTargetAuthorId(TargetType targetType, Long targetId) {
        return switch (targetType) {
            case POST -> postRepository.findById(targetId)
                    .map(Post::getMemberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
            case COMMENT -> commentRepository.findById(targetId)
                    .map(Comment::getMemberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        };
    }

    private long getLikeCount(String redisKey) {
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }
}
