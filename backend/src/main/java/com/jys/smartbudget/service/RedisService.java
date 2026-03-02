package com.jys.smartbudget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration ACCESS_TOKEN_EXP = Duration.ofMinutes(1);

    private String accessKey(String userId) { return "access:" + userId; }
    private String refreshKey(String userId) { return "refresh:" + userId; }

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
            Duration.ofDays(7)
        );
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(refreshKey(userId));
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(refreshKey(userId));
    }

    // 락 획득 시도 
    public boolean acquireLockWithRetry(String approvalNo) {
        // Todo: approvalNo로 이미 저장된 내역이 있는지 먼저 조회



        String key = "lock:payment:" + approvalNo;
        int retryCount = 5; // 최대 5번 재시도
        
        while (retryCount > 0) {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, "locked", Duration.ofSeconds(10));
            
            if (success != null && success) return true; // 락 획득 성공
            
            // 락 획득 실패 시 잠시 대기 후 재시도
            try {
                Thread.sleep(100); // 100ms 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            retryCount--;
        }
        return false; // 최종 실패 시에만 false 반환
    }

    // 락 해제
    public void releaseLock(String approvalNo) {
        String key = "lock:payment:" + approvalNo;
        redisTemplate.delete(key);
    }
}
