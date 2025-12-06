package com.jys.smartbudget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final long EXPIRATION_HOURS = 1000 * 60 * 60; ; // JWT 만료시간과 동일하게

    public void saveToken(String userId, String token) {
        redisTemplate.opsForValue().set(userId, token, Duration.ofHours(EXPIRATION_HOURS));
    }

    public String getToken(String userId) {
        return redisTemplate.opsForValue().get(userId);
    }

    public void deleteToken(String userId) {      
        redisTemplate.delete(userId);
    }
}
