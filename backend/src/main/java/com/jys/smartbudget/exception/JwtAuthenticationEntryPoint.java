package com.jys.smartbudget.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException; 
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jys.smartbudget.dto.ApiResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // 1. 에러 코드 가져오기
        ErrorCode errorCode = ErrorCode.TOKEN_INVALID; 

        // 2. HTTP 응답 설정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        // 3. ApiResponse 객체 생성 (데이터 없이 실패 응답만)
        ApiResponse<Void> apiResponse = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());

        // 4. ObjectMapper를 이용해 객체를 JSON 문자열로 변환 후 출력
        // writeValueAsString: Object -> JSON String
        String jsonResult = objectMapper.writeValueAsString(apiResponse);
        
        response.getWriter().write(jsonResult);
    }
}