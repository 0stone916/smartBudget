package com.jys.smartbudget.controller;

import java.util.HashMap;
import java.util.Map;
import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.UserDTO;
import com.jys.smartbudget.exception.BusinessException;
import com.jys.smartbudget.exception.ErrorCode;
import com.jys.smartbudget.service.RedisTokenService;
import com.jys.smartbudget.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import com.jys.smartbudget.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController  // 이 클래스가 REST API 컨트롤러임을 표시
@RequestMapping("/auth")  // 이 컨트롤러의 기본 경로는 /auth
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RedisTokenService redisTokenService;
    private final JwtUtil jwtUtil;

    /**
     * 로그인 API: POST /auth/login
     * 
     * 흐름:
     * 1. 사용자가 아이디/비밀번호 입력
     * 2. DB에서 사용자 정보 조회 및 비밀번호 검증
     * 3. 검증 성공 시 JWT 토큰 생성
     * 4. Redis에 토큰 저장 (기존 토큰 덮어쓰기 = 단일 세션)
     * 5. 토큰을 클라이언트에게 반환
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody UserDTO user) {

        // 1. UserService를 통해 로그인 검증
        // DB에서 userId로 사용자 찾고, BCrypt로 비밀번호 비교
        // 성공 시 UserDTO 반환, 실패 시 null 반환
        log.info("@@@@@@@@@@@@@@@@@@@@@@");

        UserDTO loginUser = userService.login(user.getUserId(), user.getPassword());
        
                log.info("@@@@@@@2222@@@");
        if (loginUser != null) {
            // 2. 로그인 성공 → JWT 토큰 생성
            // JwtUtil.generateToken()은 userId를 JWT에 담아 서명된 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getUserId().toString());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserId().toString());
            
            // 3. Redis에 토큰 저장
            // Key: userId, Value: 방금 생성한 토큰
            // 같은 userId로 다시 로그인하면 기존 토큰이 덮어씌워짐
            // → 이전 기기의 토큰은 자동으로 무효화됨 (단일 세션의 핵심)
            redisTokenService.saveAccessToken(loginUser.getUserId(), accessToken);
            redisTokenService.saveRefreshToken(loginUser.getUserId(), refreshToken);

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);

            // 4. 클라이언트에게 성공 응답 + 토큰 전달
            // ApiResponse: {success: true, message: "로그인 성공", data: "토큰"}
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", result));
        }

        // 5. 로그인 실패 (아이디/비밀번호 불일치)
        // 401 Unauthorized 상태코드 반환
        throw new BusinessException(ErrorCode.LOGIN_FAILED);
    }

    /**
     * 회원가입 API: POST /auth/register
     * 
     * DuplicateKeyException: MyBatis에서 UNIQUE 제약조건 위반 시 발생
     * → userId가 이미 존재하는 경우
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserDTO user) {

        // UserService에서 비밀번호 암호화 후 DB에 저장
        userService.register(user);
        
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공"));
    }

    /**
     * 로그아웃 API: POST /auth/logout
     * 
     * JWT는 기본적으로 stateless라 서버에서 강제로 무효화하기 어려움
     * → Redis에 저장된 토큰을 삭제하는 방식으로 로그아웃 구현
     * 
     * @RequestHeader: HTTP 헤더에서 값 추출
     * "Authorization: Bearer abc123" → authHeader = "Bearer abc123"
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest req) {

    // 1. 필터가 이미 토큰 검증하고 넣어준 userId만 꺼냅니다.
        String userId = (String) req.getAttribute("userId");

        // 2. 만약 여기까지 왔는데 userId가 없다면? (시큐리티 설정 오류 방어 코드)
        if (userId == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // Redis에서 해당 유저의 토큰 삭제
        // 이제 이 토큰으로 API 요청하면 JwtAuthFilter에서 차단됨
        // (Redis에 토큰이 없으므로 savedToken == null)
        redisTokenService.deleteAccessToken(userId);
        redisTokenService.deleteRefreshToken(userId);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String userId;
        try {
            userId = jwtUtil.extractUserIdAllowExpired(refreshToken);
        } catch (Exception e) {
            log.error("Refresh token parsing failed: {}", e.getMessage()); 
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "유효하지 않은 Refresh Token입니다.");
        }

        // Redis에 저장된 기존 Refresh Token 가져오기
        String storedToken = redisTokenService.getRefreshToken(userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "만료되었거나 유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");
        }

        // 새 access 발급 + redis 갱신
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        redisTokenService.saveAccessToken(userId, newAccessToken);

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);

        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", result));
    }
}