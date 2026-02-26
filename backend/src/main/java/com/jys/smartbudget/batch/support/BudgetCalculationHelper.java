package com.jys.smartbudget.batch.support;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.dto.ExpenseDTO;
import com.jys.smartbudget.dto.SearchRequest;
import com.jys.smartbudget.mapper.ExpenseMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BudgetCalculationHelper {

    private final ExpenseMapper expenseMapper;

    public List<BudgetDTO> calculateBudgets(
        String userId,
        YearMonth baseYm,
        YearMonth targetYm
    ) {

        SearchRequest condition = new SearchRequest();
        condition.setUserId(userId);
        condition.setYear(baseYm.getYear());
        condition.setMonth(baseYm.getMonthValue());

        List<ExpenseDTO> expenses =
            expenseMapper.searchExpenses(condition);

        if (expenses.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> categoryTotals =
            expenses.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getCategory() != null
                        ? e.getCategory().getCode()
                        : "UNKNOWN",
                    Collectors.summingInt(ExpenseDTO::getAmount)
                ));

        List<BudgetDTO> budgets = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
            BudgetDTO budget = new BudgetDTO();
            budget.setUserId(userId);
            budget.getCategory().setCode(entry.getKey());
            budget.setAmount(entry.getValue() + 50_000);
            budget.setYear(targetYm.getYear());
            budget.setMonth(targetYm.getMonthValue());
            budget.setDescription(
                String.format(
                    "%d년 %d월 지출 기반 자동 생성",
                    baseYm.getYear(),
                    baseYm.getMonthValue()
                )
            );
            budgets.add(budget);
        }

        return budgets;
    }
}
