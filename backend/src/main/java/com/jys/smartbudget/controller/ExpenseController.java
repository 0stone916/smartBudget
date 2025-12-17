package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.service.ExpenseService;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // 지출 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> searchExpenses(
            HttpServletRequest req,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        String userId = (String) req.getAttribute("userId");

        ExpenseDTO expense = new ExpenseDTO();
        expense.setUserId(userId);
        expense.setYear(year);
        expense.setMonth(month);

        List<ExpenseDTO> result = expenseService.searchExpenses(expense);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "지출 조회 성공", result)
        );
    }

    // 지출 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertExpense(
            HttpServletRequest req,
            @RequestBody ExpenseDTO expense) {

        try {
            String userId = (String) req.getAttribute("userId");
            expense.setUserId(userId);

            expenseService.insertExpense(expense);

            boolean overBudget = expenseService.checkOverBudget(expense);

            if (overBudget) {
                return ResponseEntity.ok(
                        new ApiResponse<>(true, "해당 예산을 초과했습니다.", null)
                );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "지출이 등록되었습니다.", null)
            );

        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ApiResponse<>(false, "지출 등록 중 오류가 발생했습니다.", null)
            );
        }
    }

    // 지출 수정
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateExpense(
            HttpServletRequest req,
            @RequestBody ExpenseDTO expense) {

        String userId = (String) req.getAttribute("userId");
        expense.setUserId(userId);

        try {
            expenseService.updateExpense(expense);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "지출 수정 완료", null)
            );
        } catch (OptimisticLockException e) {
            return ResponseEntity.ok(
                new ApiResponse<>(false, "이미 수정된 요청입니다.", null)
            );
        }
    }

    // 지출 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            HttpServletRequest req,
            @PathVariable Long id) {

        String userId = (String) req.getAttribute("userId");

        expenseService.deleteExpense(id, userId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "지출 삭제 완료", null)
        );
    }
}
