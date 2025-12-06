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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RedisTokenService redisTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody UserDTO user) {

        UserDTO loginUser = userService.login(user.getUserId(), user.getPassword());
        if (loginUser != null) {

            String token = JwtUtil.generateToken(loginUser.getUserId());

            // 기존 토큰 제거 + 새 토큰 저장 (단일기기 로그인)
            redisTokenService.saveToken(loginUser.getUserId(), token);

            return ResponseEntity.ok(new ApiResponse(true, "로그인 성공", token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "아이디 또는 비밀번호가 틀렸습니다.", null));
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserDTO user) {
        try {
            userService.register(user);
            return ResponseEntity.ok(new ApiResponse(true, "회원가입 성공", null));

        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "이미 존재하는 아이디입니다.", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "회원가입 실패: " + e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "잘못된 요청입니다.", null));
        }

        String token = authHeader.substring(7);
        String userId = JwtUtil.extractUserId(token); // JWT에서 userId 추출

        // Redis에서 해당 유저 토큰 삭제
        redisTokenService.deleteToken(userId);

        return ResponseEntity.ok(new ApiResponse(true, "로그아웃 성공", null));
}

}
