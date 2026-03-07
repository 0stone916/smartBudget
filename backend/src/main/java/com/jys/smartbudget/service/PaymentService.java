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

    @Transactional // 서비스 진입점부터 트랜잭션을 시작하는 것이 안전합니다.
    public void processPaymentNotification(NotiRequestDto notiRequestDto) {
        String approvalNo = notiRequestDto.getApprovalNo();

        // 1. 시간 데이터 가공 
        LocalDateTime transactionTime = notiRequestDto.getTransactedAt(); 
        notiRequestDto.setYear(transactionTime.getYear());
        notiRequestDto.setMonth(transactionTime.getMonthValue());
        notiRequestDto.setDay(transactionTime.getDayOfMonth());

        // 2. Redis 락 획득 (분산 환경 중복 처리 방지)
        if (!redisService.acquireLockWithRetry(approvalNo)) {
            throw new RuntimeException("중복 처리 방지를 위한 락 획득 실패: " + approvalNo);
        }

        try {
            // 3. 지출 기록 + 예산 차감
            expenseMapper.insertExpense(notiRequestDto);
            int updatedRows = budgetMapper.updateBudget(notiRequestDto);

            if (updatedRows == 0) {
                throw new RuntimeException("예산 정보 부족 혹은 누락: " + notiRequestDto.getUserId());
            }
        } finally {
            redisService.releaseLock(approvalNo);
        }
    }
}