package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.dto.SearchRequest;
import com.jys.smartbudget.service.BudgetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated          //@PathVariable이나 @RequestParam에 직접 붙인 제약 조건 사용시 필요
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
            @Valid @RequestBody BudgetDTO budget) {

        String userId = (String) req.getAttribute("userId");
        budget.setUserId(userId);

        budgetService.insertBudget(budget);
        return ResponseEntity.ok(
                ApiResponse.success("예산 등록 완료")
        );
    }

    // 예산 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> searchBudgets(
            HttpServletRequest req,
            @Valid SearchRequest searchRequest) {

        String userId = (String) req.getAttribute("userId");

        BudgetDTO budget = new BudgetDTO();
        budget.setUserId(userId);
        budget.setYear(searchRequest.getYear());
        budget.setMonth(searchRequest.getMonth());

        List<BudgetDTO> budgets =
                budgetService.selectBudgetsByConditionWithPaging(budget);

        return ResponseEntity.ok(
                ApiResponse.success("조회 성공", budgets)
        );
    }

    // 예산 수정
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateBudget(
            HttpServletRequest req,
            @Valid @RequestBody BudgetDTO budget) {

        String userId = (String) req.getAttribute("userId");
        budget.setUserId(userId);

        budgetService.updateBudget(budget);

        return ResponseEntity.ok(
                ApiResponse.success("예산 수정 완료")
        );
        
    }

    // 예산 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            HttpServletRequest req,
                @PathVariable @Min(value = 1, message = "유효하지 않은 예산 ID입니다.") Long id) {

        String userId = (String) req.getAttribute("userId");

        budgetService.deleteBudget(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success("예산 삭제 완료")
        );
    }
}
