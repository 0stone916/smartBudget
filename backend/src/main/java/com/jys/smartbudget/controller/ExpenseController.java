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
                ApiResponse.success("지출 조회 성공", result)
        );
    }

    // 지출 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertExpense(
            HttpServletRequest req,
            @RequestBody ExpenseDTO expense) {

        String userId = (String) req.getAttribute("userId");
        expense.setUserId(userId);

        expenseService.insertExpense(expense);

        boolean overBudget = expenseService.checkOverBudget(expense);

        if (overBudget) {
            return ResponseEntity.ok(
                ApiResponse.success("해당 예산을 초과했습니다.")
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success("지출 등록 완료")
        );
    }

    // 지출 수정
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateExpense(
            HttpServletRequest req,
            @RequestBody ExpenseDTO expense) {

        String userId = (String) req.getAttribute("userId");
        expense.setUserId(userId);

        expenseService.updateExpense(expense);

        return ResponseEntity.ok(
                ApiResponse.success("지출 수정 완료")
        );

    }

    // 지출 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            HttpServletRequest req,
            @PathVariable Long id) {

        String userId = (String) req.getAttribute("userId");

        expenseService.deleteExpense(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success("지출 삭제 완료")
        );
    }
}
