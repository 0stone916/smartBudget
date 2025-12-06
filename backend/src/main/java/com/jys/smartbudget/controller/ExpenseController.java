package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.service.ExpenseService;
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
    public List<ExpenseDTO> searchExpenses(
            HttpServletRequest req,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        String userId = (String) req.getAttribute("userId");

        ExpenseDTO expense = new ExpenseDTO();
        expense.setUserId(userId);
        expense.setYear(year);
        expense.setMonth(month);

        return expenseService.searchExpenses(expense);
    }

    // 지출 등록
    @PostMapping
    public ResponseEntity<ApiResponse> insertExpense(
            HttpServletRequest req,
            @RequestBody ExpenseDTO expense) {

        try {
            String userId = (String) req.getAttribute("userId");
            expense.setUserId(userId);

            expenseService.insertExpense(expense);

            boolean overBudget = expenseService.checkOverBudget(expense);
            if (overBudget) {
                return ResponseEntity.ok(
                        new ApiResponse(true, "해당 예산을 초과했습니다.", null));
            } else {
                return ResponseEntity.ok(
                        new ApiResponse(true, "지출이 등록되었습니다.", null));
            }

        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ApiResponse(false, "지출 등록 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 지출 수정
    @PutMapping
    public String updateExpense(
            HttpServletRequest req,
            @RequestBody ExpenseDTO expense) {

        String userId = (String) req.getAttribute("userId");
        expense.setUserId(userId);

        expenseService.updateExpense(expense);
        return "지출이 수정되었습니다.";
    }

    // 지출 삭제
    @DeleteMapping("/{id}")
    public String deleteExpense(
            HttpServletRequest req,
            @PathVariable Long id) {

        String userId = (String) req.getAttribute("userId");

        expenseService.deleteExpense(id, userId);
        return "지출이 삭제되었습니다.";
    }
}
