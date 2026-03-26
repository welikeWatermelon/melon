package com.melonme.like.service;

import com.melonme.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final LikeRepository likeRepository;

    private static final String LIKE_POST_KEY_PREFIX = "like:post:";
    private static final String LIKE_COMMENT_KEY_PREFIX = "like:comment:";

    /**
     * 1분마다 Redis 좋아요 카운터를 PostgreSQL에 동기화
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncLikeCountsToDatabase() {
        syncPostLikeCounts();
        syncCommentLikeCounts();
    }

    private void syncPostLikeCounts() {
        Set<String> keys = redisTemplate.keys(LIKE_POST_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int syncCount = 0;
        for (String key : keys) {
            try {
                Long postId = extractId(key, LIKE_POST_KEY_PREFIX);
                Object value = redisTemplate.opsForValue().getAndDelete(key);
                if (value == null) continue;

                int count = parseCount(value);
                likeRepository.updatePostLikeCount(postId, count);
                syncCount++;
            } catch (Exception e) {
                log.error("게시글 좋아요 동기화 실패: key={}", key, e);
            }
        }
        if (syncCount > 0) {
            log.info("게시글 좋아요 동기화 완료: {} 건", syncCount);
        }
    }

    private void syncCommentLikeCounts() {
        Set<String> keys = redisTemplate.keys(LIKE_COMMENT_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int syncCount = 0;
        for (String key : keys) {
            try {
                Long commentId = extractId(key, LIKE_COMMENT_KEY_PREFIX);
                Object value = redisTemplate.opsForValue().getAndDelete(key);
                if (value == null) continue;

                int count = parseCount(value);
                likeRepository.updateCommentLikeCount(commentId, count);
                syncCount++;
            } catch (Exception e) {
                log.error("댓글 좋아요 동기화 실패: key={}", key, e);
            }
        }
        if (syncCount > 0) {
            log.info("댓글 좋아요 동기화 완료: {} 건", syncCount);
        }
    }

    private Long extractId(String key, String prefix) {
        return Long.parseLong(key.substring(prefix.length()));
    }

    private int parseCount(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
