package com.melonme.member.service;

import com.melonme.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProvider jwtProvider;

    public void save(Long memberId, String refreshToken) {
        String key = KEY_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, refreshToken,
                jwtProvider.getRefreshTokenExpiry(), TimeUnit.MILLISECONDS);
    }

    public Optional<String> find(Long memberId) {
        String key = KEY_PREFIX + memberId;
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value).map(Object::toString);
    }

    public void delete(Long memberId) {
        String key = KEY_PREFIX + memberId;
        redisTemplate.delete(key);
    }
}
