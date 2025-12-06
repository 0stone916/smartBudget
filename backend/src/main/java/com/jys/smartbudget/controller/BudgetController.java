package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.service.BudgetService;
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
    public ResponseEntity<ApiResponse> insertBudget(
            HttpServletRequest req,
            @RequestBody BudgetDTO budget) {

        String userId = (String) req.getAttribute("userId");
        budget.setUserId(userId);

        boolean exists = budgetService.existsByYearMonthCategory(budget);
        if (exists) {
            return ResponseEntity.ok(new ApiResponse(false, "이미 등록된 카테고리입니다.", null));
        }

        budgetService.insertBudget(budget);
        return ResponseEntity.ok(new ApiResponse(true, "예산 등록 완료", null));
    }

    // 예산 조회
    @GetMapping("/search")
    public List<BudgetDTO> searchBudgets(
            HttpServletRequest req,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        String userId = (String) req.getAttribute("userId");

        BudgetDTO budget = new BudgetDTO();
        budget.setUserId(userId);
        budget.setYear(year);
        budget.setMonth(month);

        return budgetService.selectBudgetsByConditionWithPaging(budget);
    }

    // 예산 수정
    @PutMapping
    public String updateBudget(
            HttpServletRequest req,
            @RequestBody BudgetDTO budget) {

        String userId = (String) req.getAttribute("userId");
        budget.setUserId(userId);

        budgetService.updateBudget(budget);
        return "예산이 수정되었습니다.";
    }

    // 예산 삭제
    @DeleteMapping("/{id}")
    public String deleteBudget(
            HttpServletRequest req,
            @PathVariable Long id) {

        String userId = (String) req.getAttribute("userId");

        budgetService.deleteBudget(id, userId);
        return "예산이 삭제되었습니다.";
    }
}
