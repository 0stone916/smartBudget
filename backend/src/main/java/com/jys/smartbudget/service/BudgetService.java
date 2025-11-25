package com.jys.smartbudget.service;

import com.jys.smartbudget.dto.BudgetDTO;
import com.jys.smartbudget.mapper.BudgetMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetMapper budgetMapper;

    public BudgetService(BudgetMapper budgetMapper) {
        this.budgetMapper = budgetMapper;
    }

    public void insertBudget(BudgetDTO budget) {
        budgetMapper.insertBudget(budget);
    }

    public List<BudgetDTO> selectBudgetsByConditionWithPaging(BudgetDTO condition) {
        return budgetMapper.selectBudgetsByConditionWithPaging(condition);
    }

    public void updateBudget(BudgetDTO budget) {
        budgetMapper.updateBudget(budget);
    }

    public void deleteBudget(Long id, String userId) {
        budgetMapper.deleteBudget(id, userId);
    }

    public Boolean existsByYearMonthCategory(BudgetDTO budget) {
        return budgetMapper.existsByYearMonthCategory(budget);
    }


}
