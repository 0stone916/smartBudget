package com.jys.smartbudget.config;

import com.jys.smartbudget.service.RedisTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

// OncePerRequestFilter: Spring이 제공하는 필터 기본 클래스
// 한 번의 요청당 딱 한 번만 실행되도록 보장해줌 (중복 실행 방지)
// 템플릿 메서드 패턴: 부모가 전체 흐름을 제어하고, 내가 세부 구현만 채우면 됨
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // Redis에서 토큰을 조회/저장/삭제하는 서비스
    private final RedisTokenService redisTokenService;

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(RedisTokenService redisTokenService, JwtUtil jwtUtil ) {
        this.redisTokenService = redisTokenService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * shouldNotFilter: 이 필터를 "실행하지 않을" 조건을 정의
     * true 반환 → 필터 건너뛰고 바로 다음 단계로
     * false 반환 → doFilterInternal() 실행
     * 
     * 왜 필요한가? 로그인/회원가입 같은 경로는 토큰이 없어도 접근 가능해야 하니까
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // /auth/로 시작하는 경로 (로그인, 회원가입) → 필터 스킵
        // /error 경로 → Spring이 에러 처리할 때 사용, 필터 적용하면 무한루프 발생
        // OPTIONS 요청 → CORS preflight 요청, 토큰 검사 불필요
        return path.startsWith("/auth/") || 
               path.startsWith("/error") ||
               request.getMethod().equalsIgnoreCase("OPTIONS");
    }

    /**
     * doFilterInternal: 실제 필터 로직을 구현하는 메서드
     * Spring이 자동으로 호출해줌 (shouldNotFilter가 false일 때만)
     * 
     * 역할: JWT 토큰 검증 + Redis와 비교하여 최신 토큰인지 확인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {




         
        // 테스트용
        String testUser = req.getHeader("X-TEST-USER");
        if (testUser != null) {
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            req.setAttribute("userId", testUser);

            chain.doFilter(req, res);
            return;
        }        

        // 1. HTTP 헤더에서 Authorization 값 가져오기
        // 형식: "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi..."
        String authHeader = req.getHeader("Authorization");

        // // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면
        // // → 인증 실패 처리하고 여기서 종료 (Controller까지 안 감)
        // if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        //     res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태코드
        //     res.getWriter().write("Authorization Header Missing");
        //     return; // 여기서 끝! chain.doFilter() 호출 안 함 = 다음 단계로 안 감
        // }
        //Spring Security 요청권한을 확인 후 EntryPoint를 호출할 수 있게 주석

        // 2. "Bearer " 부분 제거하고 순수 토큰만 추출
        // "Bearer abc123" → "abc123"
        if (authHeader != null && authHeader.startsWith("Bearer ")) { 
            String token = authHeader.replace("Bearer ", "");
    
            try {
                // 3. JWT 토큰에서 userId 추출
                // JWT는 서명되어 있어서 위조 불가능 (SECRET_KEY로 검증)
                // 토큰이 변조되었으면 여기서 JwtException 발생
                String userId = jwtUtil.extractUserIdAllowExpired(token);
    
                // 4. Redis에서 이 사용자의 최신 토큰 가져오기
                // Redis 구조: Key=userId, Value=최신토큰
                String savedToken = redisTokenService.getAccessToken(userId);
    
                if (savedToken != null && savedToken.equals(token)) {   
                    // 5. 인증 성공 시에만 SecurityContext에 등록
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    req.setAttribute("userId", userId);
                }
    
            } catch (JwtException e) {
                // 토큰 오류 발생 시 로그만 남기고 그냥 다음 체인으로 넘깁니다.
                // 어차피 SecurityContext에 인증 정보가 없으므로 
                // SecurityConfig에서 .authenticated() 설정된 API는 EntryPoint가 잡아냅니다.
                log.error("JWT validation failed: {}", e.getMessage());
            }
    
            // 필터 체인의 다음 단계로 진행
            // 다음 필터가 있으면 다음 필터로, 없으면 Controller로
        }
        chain.doFilter(req, res);
    }
}