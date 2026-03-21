package com.jys.smartbudget.controller;

import com.jys.smartbudget.dto.ApiResponse;
import com.jys.smartbudget.dto.AccountDto;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.SearchRequest;
import com.jys.smartbudget.exception.BusinessException;
import com.jys.smartbudget.exception.ErrorCode;
import com.jys.smartbudget.service.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
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

                // 1. year, month 정보를 이용해 해당 월의 범위를 계산
                LocalDate startOfMonth = LocalDate.of(searchRequest.getYear(), searchRequest.getMonth(), 1);
                LocalDateTime startTime = startOfMonth.atStartOfDay(); // 예: 2026-03-01 00:00:00

                LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
                LocalDateTime endTime = endOfMonth.atTime(LocalTime.MAX); // 예: 2026-03-31 23:59:59

                searchRequest.setStartTime(startTime);
                searchRequest.setEndTime(endTime);

                List<ExpenseDTO> expenses = expenseService.getExpenses(searchRequest);

                String bankUrl = "http://localhost:8081/api/v1/payments/accounts?userId=" + userId;

                log.info("userIduserIduserIduserIduserId", (String) req.getAttribute("userId"));

                // RestTemplate을 통한 서버 간 통신 (Internal API Call)
                AccountDto bankAccount = restTemplate.getForObject(bankUrl, AccountDto.class);

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
