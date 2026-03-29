package com.jys.smartbudget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration ACCESS_TOKEN_EXP = Duration.ofMinutes(30);

    private String accessKey(String userId) {
        return "access:" + userId;
    }

    private String refreshKey(String userId) {
        return "refresh:" + userId;
    }

    public void saveAccessToken(String userId, String token) {
        redisTemplate.opsForValue().set(accessKey(userId), token, ACCESS_TOKEN_EXP);
    }

    public String getAccessToken(String userId) {
        return redisTemplate.opsForValue().get(accessKey(userId));
    }

    public void deleteAccessToken(String userId) {
        redisTemplate.delete(accessKey(userId));
    }

    public void saveRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                refreshKey(userId),
                refreshToken,
                Duration.ofDays(7));
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(refreshKey(userId));
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(refreshKey(userId));
    }

    public boolean acquireLock(String approvalNo) {
        String key = "lock:payment:" + approvalNo;

        // 재시도 없이 딱 한 번만 시도 (Kafka가 대신 재시도해줄 것이므로)
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", Duration.ofSeconds(10));

        return success != null && success;
    }

    // 락 해제
    public void releaseLock(String approvalNo) {
        String key = "lock:payment:" + approvalNo;
        redisTemplate.delete(key);
    }
}
