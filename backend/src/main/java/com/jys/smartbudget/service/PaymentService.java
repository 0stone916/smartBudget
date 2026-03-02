package com.jys.smartbudget.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.jys.smartbudget.dto.NotiRequestDto;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.mapper.ExpenseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ExpenseMapper expenseMapper;
    private final BudgetMapper budgetMapper;
    private final RedisService redisService;

    public void processPaymentNotification(NotiRequestDto notiRequestDto) {
        String approvalNo = (String) notiRequestDto.getApprovalNo();

        // 1. 은행에서 준 transacted_at (Timestamp) 활용
        LocalDateTime transactionTime = notiRequestDto.getTransactedAt(); 

        // 2. 인덱스 최적화를 위해 연/월/일 추출
        int year = transactionTime.getYear();
        int month = transactionTime.getMonthValue();
        int day = transactionTime.getDayOfMonth();

        notiRequestDto.setYear(year);
        notiRequestDto.setMonth(month);
        notiRequestDto.setDay(day);

        // 1. Redis 락 획득 시도
        if (!redisService.acquireLockWithRetry(approvalNo)) {
            log.warn("락 획득 실패");
            throw new RuntimeException("락 획득 실패");
        }

        try {
            // 2. 실제 DB 저장 (트랜잭션 적용)
            saveToDatabase(notiRequestDto);
        } finally {
            // 3. 작업 완료 후 반드시 락 해제
            redisService.releaseLock(approvalNo);
        }
    }

    @Transactional
    protected void saveToDatabase(NotiRequestDto notiRequestDto) {
        // 지출 내역 저장 (MyBatis)
        expenseMapper.insertExpense(notiRequestDto);
        // 부서 예산 차감 (MyBatis)
        int updatedRows = budgetMapper.updateBudget(notiRequestDto);

        if (updatedRows == 0) {
            log.error("예산 차감 실패: 유저={}, 계좌={}", notiRequestDto.getUserId(), notiRequestDto.getAccountNumber());
            throw new RuntimeException("등록된 예산 정보를 찾을 수 없거나 예산이 부족합니다.");
        }
        log.info("지출 기록 및 예산 차감 완료: {}", notiRequestDto.getApprovalNo());
    }
}