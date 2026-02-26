package com.jys.smartbudget.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key jwtKey;

    public JwtUtil(Key jwtKey) {
        //JwtUtil 은 @Component 이므로 스프링이 생성할 때 자동으로 의존성을 찾는다
        this.jwtKey = jwtKey;
    }

    /* JwtKeyConfig가 Key Bean 생성
        ↓
    Spring Container에 Key Bean 저장
        ↓
    JwtUtil 생성 시 Spring이 Key Bean을 찾아서 생성자에 넣어줌
        ↓
    JwtUtil 내부에서 jwtKey 로 JWT 서명/검증에 사용 */
    public String generateAccessToken(String userId) {
        Date now = new Date();
        Date exp = new Date(now.toInstant().plus(Duration.ofMinutes(1000)).toEpochMilli());


        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 1000L * 60 * 60 * 24 * 7); // 7일

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtKey)   
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractUserIdAllowExpired(String token) {
        try {
            return extractUserId(token); // 기존 (정상)
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            // 만료된 토큰의 claims는 exception.getClaims()로 얻을 수 있음
            return ex.getClaims().getSubject();
        }
    }
}
