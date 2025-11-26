package com.jys.smartbudget.controller;

import com.jys.smartbudget.config.JwtUtil;
import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.service.BudgetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // 예산 등록 (JWT에서 userId 추출)


    @PostMapping
    public ResponseEntity<ApiResponse> insertBudget(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BudgetDTO budget) {

        String token = authHeader.replace("Bearer ", "");
        try {
            // JWT 검증
            String userId = JwtUtil.extractUserId(token);
            budget.setUserId(userId);
        } catch (Exception e) {
            // 토큰이 변조되었거나 만료된 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "유효하지 않은 토큰입니다.", null));
        }

        boolean exists = budgetService.existsByYearMonthCategory(budget);
        if (exists) {
            return ResponseEntity.ok(
                new ApiResponse(false, "해당 년월에 이미 등록된 카테고리입니다.", null));
        }

        budgetService.insertBudget(budget);
        return ResponseEntity.ok(
            new ApiResponse(true, "예산이 등록되었습니다.", null));
    }

    // 검색 (필요시 userId 포함)
    @GetMapping("/search")
    public List<BudgetDTO> searchBudgets(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        BudgetDTO budget = new BudgetDTO();
        String token = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.extractUserId(token);
        budget.setUserId(userId);
        budget.setYear(year);
        budget.setMonth(month);

        return budgetService.selectBudgetsByConditionWithPaging(budget);
    }

    @PutMapping
    public String updateBudget(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BudgetDTO budget) {

        String token = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.extractUserId(token);
        budget.setUserId(userId);

        budgetService.updateBudget(budget);
        return "예산이 수정되었습니다.";
    }

    @DeleteMapping("/{id}")
    public String deleteBudget(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.extractUserId(token);

        budgetService.deleteBudget(id, userId);
        return "예산이 삭제되었습니다.";
    }
}
