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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController<T> {

        private final RestTemplate restTemplate;
        private final ExpenseService expenseService;
        private final Semaphore semaphore = new Semaphore(2);

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
                log.info("searchRequest", searchRequest.getAccountNumber());
                log.info("searchRequest", searchRequest.getStartTime());
                log.info("searchRequest", searchRequest.getStartTime());
                log.info("searchRequest", searchRequest.getUserId());
                log.info("searchRequest", searchRequest);

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

        @PostMapping("/searchTest")
        public ResponseEntity<ApiResponse<Void>> getExpenseWithBalanceTest(
                        @RequestBody SearchRequest searchRequest,
                        HttpServletRequest req) {

                LocalDate startOfMonth = LocalDate.of(searchRequest.getYear(), searchRequest.getMonth(), 1);
                LocalDateTime startTime = startOfMonth.atStartOfDay();
                LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
                LocalDateTime endTime = endOfMonth.atTime(LocalTime.MAX);

                searchRequest.setStartTime(startTime);
                searchRequest.setEndTime(endTime);

                // 1. 세마포어 진입 제어
                if (!semaphore.tryAcquire()) {
                        log.warn("세마포어 요청 차단 - userId: {}");
                        throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
                }

                try {
                        log.info("세마포어 획득 성공 - 로직 실행 시작 (userId: {})");

                        // 2. 비즈니스 로직 수행
                        List<ExpenseDTO> expenses = expenseService.getExpenses(searchRequest);

                        // 3. [부하 테스트]
                        List<byte[]> dummyList = new ArrayList<>();
                        for (int i = 0; i < 500; i++) {
                                dummyList.add(new byte[1024 * 1024]);
                        }

                        TimeUnit.SECONDS.sleep(10);
                        log.info("대용량 테스트 성공");

                        return ResponseEntity.ok(ApiResponse.success("대용량 테스트 성공"));

                } catch (InterruptedException e) {
                        log.error("대용량 조회 중 인터럽트 발생");
                        Thread.currentThread().interrupt();
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR); // 핸들러가 처리하도록 위임
                } catch (Throwable t) {
                        log.error("치명적 에러(OOM 등) 발생: {}", t.getMessage());
                        throw t; // 핸들러의 Exception.class에서 처리
                } finally {
                        // 4. 항상 자원 반납
                        semaphore.release();
                        log.info("세마포어 반납 완료");
                }
        }

}
