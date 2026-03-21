package com.jys.smartbudget.batch.processor;

import com.jys.smartbudget.dto.BankTransactionResponse;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.ReconciliationErrorDTO;
import com.jys.smartbudget.mapper.ExpenseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@StepScope
public class ReconciliationProcessor implements ItemProcessor<BankTransactionResponse, Object> {

    private final ExpenseMapper expenseMapper;
    private final String targetDate;
    private final String accountNumber;

    private Map<String, ExpenseDTO> cmsDataCache = new HashMap<>();

    public ReconciliationProcessor(
            ExpenseMapper expenseMapper,
            @Value("#{jobParameters['targetDate']}") String targetDate,
            @Value("#{jobParameters['accountNumber']}") String accountNumber) {
        this.expenseMapper = expenseMapper;
        this.targetDate = targetDate;
        this.accountNumber = accountNumber;
    }

    private void loadCmsData() {
        // 1. String "yyyy-MM-dd" -> LocalDate 변환
        LocalDate date = LocalDate.parse(targetDate);

        // 2. 해당 날짜의 00:00:00 ~ 23:59:59 범위 계산
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

        // 3. Mapper 호출 (계좌번호와 시간 범위를 파라미터로 전달)
        List<ExpenseDTO> expenseList = expenseMapper.findExpensesByCondition(
                accountNumber, startDateTime, endDateTime);

        log.info("[Processor] CMS 캐시 로딩 - 대상: {}, 기간: {} ~ {}, 데이터: {}건",
                accountNumber, startDateTime, endDateTime, expenseList.size());

        for (ExpenseDTO expense : expenseList) {
            if (expense.getApprovalNo() != null) {
                cmsDataCache.put(expense.getApprovalNo().trim().toUpperCase(), expense);
            }
        }
    }

    @Override
    public Object process(BankTransactionResponse bankData) throws Exception {
        if (cmsDataCache.isEmpty()) {
            loadCmsData();
        }

        String bankApprovalNo = bankData.approval_no().trim().toUpperCase();
        ExpenseDTO cmsData = cmsDataCache.get(bankApprovalNo);

        if (cmsData == null) {
            log.warn("[MISSING] 은행 승인번호 {}가 CMS에 존재하지 않음", bankApprovalNo);
            return createErrorLog(bankData, null, "MISSING");
        }

        if (!cmsData.getAmount().equals(bankData.amount())) {
            log.error("[MISMATCH] 승인번호 {}: 은행({}) vs CMS({})",
                    bankApprovalNo, bankData.amount(), cmsData.getAmount());
            return createErrorLog(bankData, cmsData.getAmount(), "MISMATCH");
        }

        cmsData.setStatus("VERIFIED");
        return cmsData;
    }

    private ReconciliationErrorDTO createErrorLog(BankTransactionResponse bank, Long cmsAmount, String type) {
        return ReconciliationErrorDTO.builder()
                .approvalNo(bank.approval_no())
                .errorType(type)
                .bankAmount(bank.amount())
                .cmsAmount(cmsAmount)
                .build();
    }
}