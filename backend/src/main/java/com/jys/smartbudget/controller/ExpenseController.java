package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.SearchRequest;
import com.jys.smartbudget.dto.StatisticsDTO;
import com.jys.smartbudget.service.ExpenseService;
import com.jys.smartbudget.util.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated          //@PathVariable이나 @RequestParam에 직접 붙인 제약 조건 사용시 필요
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/insert")
    public String insertData() {
        expenseService.insertDummyData();
        return "데이터 삽입 완료!";
    }

    // 지출 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> searchExpenses(
            HttpServletRequest req,
            @Valid SearchRequest searchRequest) {

        String userId = (String) req.getAttribute("userId");
        searchRequest.setUserId(userId);


        // ExpenseDTO expense = new ExpenseDTO();
        // expense.setUserId(userId);
        // expense.setYear(searchRequest.getYear());
        // expense.setMonth(searchRequest.getMonth());

        List<ExpenseDTO> result = expenseService.searchExpenses(searchRequest);


        return ResponseEntity.ok(
                ApiResponse.success("지출 조회 성공", result)
        );
    }

    // 전체 지출 조회
    @GetMapping("/getExpenseStatistics")
    public ResponseEntity<ApiResponse<List<StatisticsDTO>>> getExpenseStatistics(
            HttpServletRequest req,
            @Valid SearchRequest searchRequest) {

        String userId = (String) req.getAttribute("userId");
        searchRequest.setUserId(userId);


        // ExpenseDTO expense = new ExpenseDTO();
        // expense.setUserId(userId);
        // expense.setYear(searchRequest.getYear());
        // expense.setMonth(searchRequest.getMonth());

        List<StatisticsDTO> result = expenseService.getExpenseStatistics(searchRequest);


        return ResponseEntity.ok(
                ApiResponse.success("지출 조회 성공", result)
        );
    }

    // 지출 등록
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> insertExpense(
            HttpServletRequest req,
            @Valid @RequestBody ExpenseDTO expense) {

        DateUtils.validateDate(expense.getYear(), expense.getMonth(), expense.getDay());

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
            @Valid @RequestBody ExpenseDTO expense) {

        DateUtils.validateDate(expense.getYear(), expense.getMonth(), expense.getDay());
        
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
                @PathVariable @Min(value = 1, message = "유효하지 않은 예산 ID입니다.") Long id) {


        String userId = (String) req.getAttribute("userId");

        expenseService.deleteExpense(id, userId);

        return ResponseEntity.ok(
                ApiResponse.success("지출 삭제 완료")
        );
    }






}
