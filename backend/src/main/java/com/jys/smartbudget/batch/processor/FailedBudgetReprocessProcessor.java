package com.jys.smartbudget.batch.processor;

import java.time.YearMonth;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.jys.smartbudget.batch.support.BudgetCalculationHelper;
import com.jys.smartbudget.dto.BatchBudgetFailHistory;
import com.jys.smartbudget.dto.BudgetDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedBudgetReprocessProcessor
        implements ItemProcessor<BatchBudgetFailHistory, List<BudgetDTO>> {

    private final BudgetCalculationHelper budgetCalculationHelper;

    @Override
    public List<BudgetDTO> process(BatchBudgetFailHistory history) {

        YearMonth targetYm =
            YearMonth.of(history.getYear(), history.getMonth());
        YearMonth baseYm = targetYm.minusMonths(1);

        log.info(
            "REPROCESS_START user={} baseYm={} targetYm={}",
            history.getUserId(),
            baseYm,
            targetYm
        );

        List<BudgetDTO> budgets =
            budgetCalculationHelper.calculateBudgets(
                history.getUserId(),
                baseYm,
                targetYm
            );

        return budgets.isEmpty() ? null : budgets;
    }
}
