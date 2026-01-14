package com.jys.smartbudget.batch.listener;

import java.util.List;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.stereotype.Component;
import com.jys.smartbudget.dto.BatchBudgetFailHistory;
import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.mapper.BatchBudgetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchSkipListener {

    private final BatchBudgetMapper batchBudgetMapper;

    @OnSkipInWrite
    public void onSkipInWrite(
        List<BudgetDTO> item,
        Throwable t
    ) {
        for (BudgetDTO budget : item) {
            batchBudgetMapper.insertBatchBudgetFailHistory(
                BatchBudgetFailHistory.builder()
                    .jobName("monthlyBudgetJob")
                    .userId(budget.getUserId())
                    .year(budget.getYear())
                    .month(budget.getMonth())
                    .category(budget.getCategory().getCode())
                    .reason(t.getMessage())
                    .build()
            );

            log.error(
                "예산 배치 실패 (user={}, {}-{}, {})",
                budget.getUserId(),
                budget.getYear(),
                budget.getMonth(),
                budget.getCategory().getCode(),
                t
            );
        }
    }
}
