package com.jys.smartbudget.controller;

import com.jys.smartbudget.config.JwtUtil;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.service.ExpenseService;
import org.springframework.web.bind.annotation.*;
import com.jys.smartbudget.dto.ApiResponse;
import org.springframework.http.ResponseEntity;


import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

        // 검색 (필요시 userId 포함)
    @GetMapping("/search")
    public List<ExpenseDTO> searchExpenses(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        ExpenseDTO expense = new ExpenseDTO();
        String token = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.extractUserId(token);
        expense.setUserId(userId);
        expense.setYear(year);
        expense.setMonth(month);

        return expenseService.searchExpenses(expense);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> insertExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ExpenseDTO expense) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = JwtUtil.extractUserId(token);
            expense.setUserId(userId);

            // DB insert 시도
            expenseService.insertExpense(expense);

            // 예산 초과 체크
            boolean overBudget = expenseService.checkOverBudget(expense);
            if (overBudget) {
                return ResponseEntity.ok(
                    new ApiResponse(true, "해당 예산을 초과했습니다.", null));
            } else {
                return ResponseEntity.ok(
                    new ApiResponse(true, "지출이 등록되었습니다", null));
            }

        } catch (Exception e) {
            // insert 실패 혹은 다른 예외 발생 시
            return ResponseEntity.ok(
                new ApiResponse(false, "지출 등록 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }


    // 지출 수정
    @PutMapping
    public String updateExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ExpenseDTO expense) {

        String token = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.extractUserId(token);
        expense.setUserId(userId);

        expenseService.updateExpense(expense);
        return "지출이 수정되었습니다.";
    }

    // 지출 삭제 (PK + userId 체크)
    @DeleteMapping("/{id}")
    public String deleteExpense(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.extractUserId(token);

        expenseService.deleteExpense(id, userId);
        return "지출이 삭제되었습니다.";
    }

}
