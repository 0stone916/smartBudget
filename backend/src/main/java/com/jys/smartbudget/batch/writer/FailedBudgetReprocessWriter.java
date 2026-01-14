package com.jys.smartbudget.batch.writer;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.mapper.BatchBudgetMapper;
import com.jys.smartbudget.mapper.BudgetMapper;
import com.jys.smartbudget.service.BudgetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedBudgetReprocessWriter
        implements ItemWriter<List<BudgetDTO>> {

    private final BudgetMapper budgetMapper;
    private final BudgetService budgetService;
    private final BatchBudgetMapper batchBudgetMapper;

    @Override
    public void write(Chunk<? extends List<BudgetDTO>> chunk) {

        for (List<BudgetDTO> budgets : chunk) {
            if (budgets == null) continue;

            for (BudgetDTO budget : budgets) {

                // 이미 생성된 예산이면 skip
                if (budgetService.existsByYearMonthCategory(budget)) {
                    log.info(
                        "REPROCESS_SKIP_ALREADY_EXISTS user={} {}-{} {}",
                        budget.getUserId(),
                        budget.getYear(),
                        budget.getMonth(),
                        budget.getCategory().getCode()
                    );
                    continue;
                }

                budgetMapper.insertBudget(budget);

                // 실패 이력 processed 처리
                batchBudgetMapper.markFailHistoryProcessed(
                    budget.getUserId(),
                    budget.getYear(),
                    budget.getMonth(),
                    budget.getCategory().getCode(),
                    LocalDateTime.now()
                );

                log.info(
                    "REPROCESS_SUCCESS user={} {}-{} {} amount={}",
                    budget.getUserId(),
                    budget.getYear(),
                    budget.getMonth(),
                    budget.getCategory().getCode(),
                    budget.getAmount()
                );
            }
        }
    }
}
