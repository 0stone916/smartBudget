package com.jys.smartbudget.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Configuration
public class JwtKeyConfig {

    // 환경변수 JWT_SECRET에서 값을 읽음
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public Key jwtSigningKey() {
        //Key 객체를 만들어서 Spring Bean으로 등록한다.
        //Bean 이름: jwtSigningKey
        //Bean 타입: java.security.Key
        
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET 환경변수가 설정되지 않았거나 32자 미만입니다."
            );
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)); //서명(Sign)**하거나 **검증(Verify) 시 사용
    }
}
