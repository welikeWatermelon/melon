package com.melonme.post.service;

import com.melonme.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Redis 조회수 -> DB 배치 동기화 (5분마다)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private static final String VIEW_COUNT_KEY_PREFIX = "view:post:";
    private static final String VIEW_COUNT_KEY_PATTERN = "view:post:*";

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 300_000) // 5분마다
    @Transactional
    public void syncViewCountToDb() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY_PATTERN);
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int syncCount = 0;
        for (String key : keys) {
            try {
                Long postId = extractPostId(key);
                Object value = redisTemplate.opsForValue().getAndDelete(key);
                if (value == null) continue;

                int count = Integer.parseInt(value.toString());
                if (count > 0) {
                    postRepository.incrementViewCount(postId, count);
                    syncCount++;
                }
            } catch (Exception e) {
                log.error("조회수 동기화 실패: key={}", key, e);
            }
        }

        if (syncCount > 0) {
            log.info("조회수 동기화 완료: {}건", syncCount);
        }
    }

    private Long extractPostId(String key) {
        return Long.parseLong(key.replace(VIEW_COUNT_KEY_PREFIX, ""));
    }
}
