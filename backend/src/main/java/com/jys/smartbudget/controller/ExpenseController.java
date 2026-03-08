package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.BankAccountDto;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.SearchRequest;
import com.jys.smartbudget.exception.BusinessException;
import com.jys.smartbudget.exception.ErrorCode;
import com.jys.smartbudget.service.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

        private final RestTemplate restTemplate;
        private final ExpenseService expenseService;

        @PostMapping("/search")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getExpenseWithBalance(
                @RequestBody SearchRequest searchRequest,
                HttpServletRequest req) {
        
                String userId = (String) req.getAttribute("userId");
                searchRequest.setUserId(userId);

                List<ExpenseDTO> expenses = expenseService.getExpenses(searchRequest);

                String bankUrl = "http://localhost:8081/api/v1/payments/accounts?userId=" + userId;
                
                log.info("userIduserIduserIduserIduserId", (String) req.getAttribute("userId"));

                // RestTemplate을 통한 서버 간 통신 (Internal API Call)
                BankAccountDto bankAccount = restTemplate.getForObject(bankUrl, BankAccountDto.class);

                if (bankAccount == null) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "연동된 은행 계좌를 찾을 수 없습니다.");
                }

                // 3. 결과 합치기
                Map<String, Object> result = new HashMap<>();
                result.put("expenses", expenses);
                result.put("accountInfo", bankAccount); // 현재 잔액 정보
                return ResponseEntity.ok(ApiResponse.success("내역 조회 성공", result));

        }

}
