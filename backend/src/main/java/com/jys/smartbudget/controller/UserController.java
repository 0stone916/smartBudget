package com.jys.smartbudget.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @PostMapping("/changeAutoBudgetPolicy")
    public ResponseEntity<ApiResponse<Void>> changeAutoBudgetPolicy(HttpServletRequest req, @RequestBody Boolean autoEnabled) {

        String userId = (String) req.getAttribute("userId");

        userService.changeAutoBudgetPolicy(autoEnabled, userId);

        return ResponseEntity.ok(
                ApiResponse.success("자동 예산 생성 여부 변경 완료")
        );
    }
}
