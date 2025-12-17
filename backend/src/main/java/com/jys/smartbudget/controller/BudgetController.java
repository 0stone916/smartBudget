package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.service.BudgetService;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
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

    // 예산 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertBudget(
            HttpServletRequest req,
            @RequestBody BudgetDTO budget) {

        String userId = (String) req.getAttribute("userId");
        budget.setUserId(userId);

        if (budgetService.existsByYearMonthCategory(budget)) {
            return ResponseEntity.ok(
                new ApiResponse<>(false, "이미 등록된 카테고리입니다.", null)
            );
        }

        budgetService.insertBudget(budget);
        return ResponseEntity.ok(
            new ApiResponse<>(true, "예산 등록 완료", null)
        );
    }

    // 예산 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> searchBudgets(
            HttpServletRequest req,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        String userId = (String) req.getAttribute("userId");

        BudgetDTO condition = new BudgetDTO();
        condition.setUserId(userId);
        condition.setYear(year);
        condition.setMonth(month);

        List<BudgetDTO> budgets =
                budgetService.selectBudgetsByConditionWithPaging(condition);

        return ResponseEntity.ok(
            new ApiResponse<>(true, "조회 성공", budgets)
        );
    }

    // 예산 수정
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateBudget(
            HttpServletRequest req,
            @RequestBody BudgetDTO budget) {

        String userId = (String) req.getAttribute("userId");
        budget.setUserId(userId);

        try {
            budgetService.updateBudget(budget);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "예산 수정 완료", null)
            );
        } catch (OptimisticLockException e) {
            return ResponseEntity.ok(
                new ApiResponse<>(false, "이미 수정된 요청입니다.", null)
            );
        }
    }

    // 예산 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            HttpServletRequest req,
            @PathVariable Long id) {

        String userId = (String) req.getAttribute("userId");

        budgetService.deleteBudget(id, userId);

        return ResponseEntity.ok(
            new ApiResponse<>(true, "예산 삭제 완료", null)
        );
    }
}
