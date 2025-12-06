package com.jys.smartbudget.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.UserDTO;
import com.jys.smartbudget.service.RedisTokenService;
import com.jys.smartbudget.service.UserService;
import com.jys.smartbudget.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController  // 이 클래스가 REST API 컨트롤러임을 표시
@RequestMapping("/auth")  // 이 컨트롤러의 기본 경로는 /auth
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RedisTokenService redisTokenService;

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
        UserDTO loginUser = userService.login(user.getUserId(), user.getPassword());
        
        if (loginUser != null) {
            // 2. 로그인 성공 → JWT 토큰 생성
            // JwtUtil.generateToken()은 userId를 JWT에 담아 서명된 토큰 생성
            String token = JwtUtil.generateToken(loginUser.getUserId());

            // 3. Redis에 토큰 저장
            // Key: userId, Value: 방금 생성한 토큰
            // 같은 userId로 다시 로그인하면 기존 토큰이 덮어씌워짐
            // → 이전 기기의 토큰은 자동으로 무효화됨 (단일 세션의 핵심)
            redisTokenService.saveToken(loginUser.getUserId(), token);

            // 4. 클라이언트에게 성공 응답 + 토큰 전달
            // ApiResponse: {success: true, message: "로그인 성공", data: "토큰"}
            return ResponseEntity.ok(new ApiResponse(true, "로그인 성공", token));
        }

        // 5. 로그인 실패 (아이디/비밀번호 불일치)
        // 401 Unauthorized 상태코드 반환
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "아이디 또는 비밀번호가 틀렸습니다.", null));
    }

    /**
     * 회원가입 API: POST /auth/register
     * 
     * DuplicateKeyException: MyBatis에서 UNIQUE 제약조건 위반 시 발생
     * → userId가 이미 존재하는 경우
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserDTO user) {
        try {
            // UserService에서 비밀번호 암호화 후 DB에 저장
            userService.register(user);
            return ResponseEntity.ok(new ApiResponse(true, "회원가입 성공", null));

        } catch (DuplicateKeyException e) {
            // userId가 이미 존재하는 경우
            // 409 Conflict 상태코드 반환
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "이미 존재하는 아이디입니다.", null));

        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "회원가입 실패: " + e.getMessage(), null));
        }
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
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {

        // Authorization 헤더 검증
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "잘못된 요청입니다.", null));
        }

        // "Bearer " 제거하고 순수 토큰만 추출
        // substring(7): "Bearer " 는 7글자
        String token = authHeader.substring(7);
        
        // 토큰에서 userId 추출
        String userId = JwtUtil.extractUserId(token);

        // Redis에서 해당 유저의 토큰 삭제
        // 이제 이 토큰으로 API 요청하면 JwtAuthFilter에서 차단됨
        // (Redis에 토큰이 없으므로 savedToken == null)
        redisTokenService.deleteToken(userId);

        return ResponseEntity.ok(new ApiResponse(true, "로그아웃 성공", null));
    }
}