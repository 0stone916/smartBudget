package com.jys.smartbudget.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.jys.smartbudget.dto.NotiRequestDto;
import com.jys.smartbudget.mapper.ExpenseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ExpenseMapper expenseMapper;
    private final RedisService redisService;

    // 락과 트랜잭션의 분리(레이스 경합 방지)
    public void processPaymentNotification(NotiRequestDto notiRequestDto) {

        LocalDateTime transactionTime = notiRequestDto.getTransactedAt();
        // notiRequestDto.setYear(transactionTime.getYear());
        // notiRequestDto.setMonth(transactionTime.getMonthValue());
        // notiRequestDto.setDay(transactionTime.getDayOfMonth());

        String approvalNo = notiRequestDto.getApprovalNo();

        // 1. 락 획득 시도
        if (!redisService.acquireLock(approvalNo)) {
            log.warn(">>>> 아직 락이 걸려있습니다. 대기 중... (승인번호: {})", approvalNo);
            throw new RuntimeException("Lock failed");
        }

        try {
            // 2. 실제 DB 작업만 트랜잭션이 걸린 별도 메서드 호출
            saveExpenseWithTransaction(notiRequestDto);
            log.info(">>>> 지출 등록 최종 성공! 승인번호: {}", approvalNo);
        } finally {
            redisService.releaseLock(approvalNo);
        }
    }

    @Transactional
    public void saveExpenseWithTransaction(NotiRequestDto notiRequestDto) {
        expenseMapper.insertExpense(notiRequestDto);
    }
}