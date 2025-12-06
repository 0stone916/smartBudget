package com.jys.smartbudget.config;

import com.jys.smartbudget.service.RedisTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends HttpFilter {     //HttpFilter는 Java 표준 서블릿 필터의 기본 클래스, 모든 요청을 가로채서 처리할 수 있음

    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)     //FilterChain: 현재 필터 작업이 끝나면 다음 필터 또는 Controller 에게 요청을 넘겨주는 역할
            throws IOException, ServletException {

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Authorization Header Missing");
            return;
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            String userId = JwtUtil.extractUserId(token);

            String savedToken = redisTokenService.getToken(userId);

            if (savedToken == null || !savedToken.equals(token)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("다른 기기에서 로그인되어 세션이 만료되었습니다.");
                return;
            }

            // userId 를 Controller 에 전달 (request attribute)
            req.setAttribute("userId", userId);

        } catch (JwtException e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("유효하지 않은 토큰입니다.");
            return;
        }

        chain.doFilter(req, res);       //현재 필터의 작업을 모두 마쳤으니 다음 단계로 넘긴다
    }
}
